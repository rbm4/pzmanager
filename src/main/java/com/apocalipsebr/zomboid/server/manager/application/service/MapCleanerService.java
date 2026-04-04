package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.MapIndex;
import com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.SafehouseInfo;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.BIN_TILE_SIZE;

@Service
public class MapCleanerService {

    private static final Logger log = LoggerFactory.getLogger(MapCleanerService.class);
    /**
     * Server-side minimum margin (in tiles) around each safehouse that is always protected.
     * This ensures bins adjacent to safehouses are never deleted, even if the client sends them.
     */
    private static final int SERVER_SAFEHOUSE_MARGIN = 16;
    /**
     * Server-side minimum margin (in tiles) around each claimed car that is always protected.
     * Cars are single points, so the margin defines the full protected radius.
     */
    private static final int SERVER_CAR_MARGIN = 8;

    private final MapDataService mapDataService;
    private final ClaimedCarRepository claimedCarRepository;

    public MapCleanerService(MapDataService mapDataService, ClaimedCarRepository claimedCarRepository) {
        this.mapDataService = mapDataService;
        this.claimedCarRepository = claimedCarRepository;
    }

    // --- Public API ---

    public MapIndex buildIndex() {
        return mapDataService.buildIndex();
    }

    public String getMapFolderPath() {
        return mapDataService.getMapFolderPath();
    }

    /**
     * Deletes the specified .bin files from the map folder.
     * Input: list of "bx/by" keys (folder/file).
     * Server-side safehouse protection: bins overlapping any safehouse (plus margin) are refused.
     * Returns a result with count of deleted files, protected skips, and any errors.
     */
    public DeleteResult deleteBins(List<String> binKeys) {
        Path mapDir = mapDataService.getMapDir();
        if (mapDir == null) {
            String path = mapDataService.getMapFolderPath();
            String msg = (path == null || path.isBlank())
                    ? "server.map.folder is not configured."
                    : "Map folder does not exist: " + path;
            return new DeleteResult(false, 0, 0, 0, 0, msg);
        }

        // Build server-side safehouse protection set
        List<String> metaWarnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(metaWarnings);
        Set<String> safehouseProtectedBins = buildProtectedBinKeys(safehouses, SERVER_SAFEHOUSE_MARGIN);

        log.info("Server-side safehouse protection: {} safehouses, {} protected bins (margin={})",
                safehouses.size(), safehouseProtectedBins.size(), SERVER_SAFEHOUSE_MARGIN);

        // Build server-side claimed car protection set
        List<ClaimedCar> claimedCars = claimedCarRepository.findByXNotNullAndYNotNull();
        Set<String> carProtectedBins = buildProtectedCarBinKeys(claimedCars, SERVER_CAR_MARGIN);

        log.info("Server-side car protection: {} claimed cars, {} protected bins (margin={})",
                claimedCars.size(), carProtectedBins.size(), SERVER_CAR_MARGIN);

        int deleted = 0;
        int notFound = 0;
        int safehouseProtectedSkips = 0;
        int carProtectedSkips = 0;
        List<String> errors = new ArrayList<>();

        for (String key : binKeys) {
            String[] parts = key.split("/");
            if (parts.length != 2) {
                errors.add("Invalid key format: " + key);
                continue;
            }

            try {
                int bx = Integer.parseInt(parts[0]);
                int by = Integer.parseInt(parts[1]);

                // Server-side safehouse protection: refuse to delete protected bins
                if (safehouseProtectedBins.contains(key)) {
                    safehouseProtectedSkips++;
                    log.debug("Safehouse-protected bin skipped: {}", key);
                    continue;
                }

                // Server-side claimed car protection: refuse to delete protected bins
                if (carProtectedBins.contains(key)) {
                    carProtectedSkips++;
                    log.debug("Car-protected bin skipped: {}", key);
                    continue;
                }

                Path binFile = mapDir.resolve(String.valueOf(bx)).resolve(by + ".bin");

                // Security: ensure the path stays within the map folder
                if (!binFile.normalize().startsWith(mapDir.normalize())) {
                    errors.add("Path traversal blocked: " + key);
                    continue;
                }

                if (Files.exists(binFile)) {
                    Files.delete(binFile);
                    deleted++;
                } else {
                    notFound++;
                }
            } catch (NumberFormatException e) {
                errors.add("Non-numeric key: " + key);
            } catch (IOException e) {
                errors.add("Failed to delete " + key + ": " + e.getMessage());
            }
        }

        // Clean up empty folders
        cleanEmptyFolders(mapDir);

        String message = String.format("Deleted %d files (%d not found, %d safehouse-protected, %d car-protected, %d errors).",
                deleted, notFound, safehouseProtectedSkips, carProtectedSkips, errors.size());
        if (!errors.isEmpty()) {
            message += " Errors: " + String.join("; ", errors.subList(0, Math.min(10, errors.size())));
        }

        log.info("Map cleaner delete: {} deleted, {} not found, {} safehouse-protected, {} car-protected, {} errors out of {} requested",
                deleted, notFound, safehouseProtectedSkips, carProtectedSkips, errors.size(), binKeys.size());

        return new DeleteResult(errors.isEmpty(), deleted, binKeys.size(), safehouseProtectedSkips, carProtectedSkips, message);
    }

    /**
     * Builds a set of "bx/by" bin keys that are protected by safehouses.
     * Each safehouse is expanded by the given margin (in tiles) on all sides.
     */
    private Set<String> buildProtectedBinKeys(List<SafehouseInfo> safehouses, int marginTiles) {
        Set<String> protectedKeys = new HashSet<>();
        for (SafehouseInfo sh : safehouses) {
            // Convert safehouse tile coords to bin coords, rounding outward
            int x1 = Math.floorDiv(sh.x() - marginTiles, BIN_TILE_SIZE);
            int y1 = Math.floorDiv(sh.y() - marginTiles, BIN_TILE_SIZE);
            int x2 = ceilDiv(sh.x() + sh.w() + marginTiles, BIN_TILE_SIZE);
            int y2 = ceilDiv(sh.y() + sh.h() + marginTiles, BIN_TILE_SIZE);

            for (int bx = x1; bx <= x2; bx++) {
                for (int by = y1; by <= y2; by++) {
                    protectedKeys.add(bx + "/" + by);
                }
            }
        }
        return protectedKeys;
    }

    /**
     * Builds a set of "bx/by" bin keys that are protected by claimed cars.
     * Each car is a single point expanded by the given margin (in tiles) on all sides.
     */
    private Set<String> buildProtectedCarBinKeys(List<ClaimedCar> cars, int marginTiles) {
        Set<String> protectedKeys = new HashSet<>();
        for (ClaimedCar car : cars) {
            if (car.getX() == null || car.getY() == null) continue;
            int tileX = (int) Math.floor(car.getX());
            int tileY = (int) Math.floor(car.getY());

            // Car is a point (w=0, h=0), so margin expands from the point in all directions
            int x1 = Math.floorDiv(tileX - marginTiles, BIN_TILE_SIZE);
            int y1 = Math.floorDiv(tileY - marginTiles, BIN_TILE_SIZE);
            int x2 = ceilDiv(tileX + marginTiles, BIN_TILE_SIZE);
            int y2 = ceilDiv(tileY + marginTiles, BIN_TILE_SIZE);

            for (int bx = x1; bx <= x2; bx++) {
                for (int by = y1; by <= y2; by++) {
                    protectedKeys.add(bx + "/" + by);
                }
            }
        }
        return protectedKeys;
    }

    /** Integer ceiling division (works correctly for negative dividends). */
    private static int ceilDiv(int a, int b) {
        return MapDataService.ceilDiv(a, b);
    }

    private void cleanEmptyFolders(Path mapDir) {
        try (DirectoryStream<Path> folders = Files.newDirectoryStream(mapDir)) {
            for (Path folder : folders) {
                if (!Files.isDirectory(folder)) continue;
                String name = folder.getFileName().toString();
                if (!name.matches("\\d+")) continue;

                try (DirectoryStream<Path> contents = Files.newDirectoryStream(folder)) {
                    if (!contents.iterator().hasNext()) {
                        Files.delete(folder);
                        log.debug("Removed empty map folder: {}", name);
                    }
                }
            }
        } catch (IOException e) {
            log.warn("Error cleaning empty folders: {}", e.getMessage());
        }
    }

    // --- Records ---

    public record DeleteResult(boolean success, int deletedCount, int requestedCount, int protectedCount,
                                int carProtectedCount, String message) {
    }
}
