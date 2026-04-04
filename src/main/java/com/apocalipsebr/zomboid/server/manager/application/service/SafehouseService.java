package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.SafehouseInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.BIN_TILE_SIZE;

@Service
public class SafehouseService {

    private static final Logger log = LoggerFactory.getLogger(SafehouseService.class);
    private static final int DEFAULT_MARGIN = 2;
    /** Z-levels to include for each chunk: -1 (basement) through +4 (upper floors). */
    private static final int Z_MIN = -1;
    private static final int Z_MAX = 4;

    private final MapDataService mapDataService;

    public SafehouseService(MapDataService mapDataService) {
        this.mapDataService = mapDataService;
    }

    public SafehouseListResult listSafehouses() {
        List<String> warnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(warnings);
        return new SafehouseListResult(safehouses, warnings);
    }

    /**
     * Writes a ZIP to the given output stream containing:
     * - map_meta.bin at the root
     * - vehicles.db and vehicles.db-journal at the root (vehicle data)
     * - map/{bx}/{by}.bin (z=0) and map/{bx}/{by}_{z}.bin (z=-1..+4) for each bin around detected safehouses
     *
     * @param marginTiles number of tiles around each safehouse to include (default 2)
     * @param out         the output stream to write the ZIP to
     * @return metadata about the export
     */
    public ExportResult exportSafehouseBinsAsZip(int marginTiles, OutputStream out) throws IOException {
        if (marginTiles < 0) marginTiles = DEFAULT_MARGIN;

        Path mapDir = mapDataService.getMapDir();
        if (mapDir == null) {
            throw new IllegalStateException("Map folder is not configured or does not exist.");
        }

        Path metaPath = mapDataService.getMetaPath();

        List<String> warnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(warnings);

        if (safehouses.isEmpty()) {
            throw new IllegalStateException("No safehouses detected. " + String.join("; ", warnings));
        }

        // Compute the set of bin keys around all safehouses
        Set<String> binKeys = buildSafehouseBinKeys(safehouses, marginTiles);

        log.info("Safehouse export: {} safehouses, {} bin keys (margin={})", safehouses.size(), binKeys.size(), marginTiles);

        int binsWritten = 0;
        long totalBytes = 0;
        List<String> missing = new ArrayList<>();

        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            // Include save-root files
            Path saveRoot = mapDir.getParent();
            totalBytes += addRootFileToZip(zos, metaPath, "map_meta.bin", warnings);
            if (saveRoot != null) {
                totalBytes += addRootFileToZip(zos, saveRoot.resolve("vehicles.db"), "vehicles.db", warnings);
                totalBytes += addRootFileToZip(zos, saveRoot.resolve("vehicles.db-journal"), "vehicles.db-journal", warnings);
            }

            // Include bin files for each z-level: map/{bx}/{by}.bin (z=0), map/{bx}/{by}_{z}.bin (z!=0)
            for (String key : binKeys) {
                String[] parts = key.split("/");
                int bx = Integer.parseInt(parts[0]);
                int by = Integer.parseInt(parts[1]);

                for (int z = Z_MIN; z <= Z_MAX; z++) {
                    String fileName = (z == 0) ? by + ".bin" : by + "_" + z + ".bin";
                    Path binFile = mapDir.resolve(String.valueOf(bx)).resolve(fileName);

                    // Security: ensure the path stays within the map folder
                    if (!binFile.normalize().startsWith(mapDir.normalize())) {
                        warnings.add("Path traversal blocked: " + key + " z=" + z);
                        continue;
                    }

                    if (Files.exists(binFile)) {
                        byte[] binData = Files.readAllBytes(binFile);
                        zos.putNextEntry(new ZipEntry("map/" + bx + "/" + fileName));
                        zos.write(binData);
                        zos.closeEntry();
                        binsWritten++;
                        totalBytes += binData.length;
                    } else if (z == 0) {
                        missing.add(key);
                    }
                }
            }

            zos.finish();
        }

        if (!missing.isEmpty()) {
            log.debug("Safehouse export: {} bins not found on disk (expected — not all bins exist)", missing.size());
        }

        log.info("Safehouse export complete: {} bins written, {} bytes total", binsWritten, totalBytes);

        return new ExportResult(safehouses.size(), binKeys.size(), binsWritten, totalBytes, warnings);
    }

    /**
     * Adds a save-root file to the ZIP if it exists, returns bytes written.
     */
    private long addRootFileToZip(ZipOutputStream zos, Path filePath, String zipName, List<String> warnings) throws IOException {
        if (filePath != null && Files.exists(filePath)) {
            byte[] data = Files.readAllBytes(filePath);
            zos.putNextEntry(new ZipEntry(zipName));
            zos.write(data);
            zos.closeEntry();
            return data.length;
        }
        warnings.add(zipName + " not found — not included in export.");
        return 0;
    }

    /**
     * Builds a set of "bx/by" bin keys around all safehouses expanded by the given margin.
     */
    private Set<String> buildSafehouseBinKeys(List<SafehouseInfo> safehouses, int marginTiles) {
        Set<String> keys = new LinkedHashSet<>();
        for (SafehouseInfo sh : safehouses) {
            int x1 = Math.floorDiv(sh.x() - marginTiles, BIN_TILE_SIZE);
            int y1 = Math.floorDiv(sh.y() - marginTiles, BIN_TILE_SIZE);
            int x2 = MapDataService.ceilDiv(sh.x() + sh.w() + marginTiles, BIN_TILE_SIZE);
            int y2 = MapDataService.ceilDiv(sh.y() + sh.h() + marginTiles, BIN_TILE_SIZE);

            for (int bx = x1; bx <= x2; bx++) {
                for (int by = y1; by <= y2; by++) {
                    keys.add(bx + "/" + by);
                }
            }
        }
        return keys;
    }

    // --- Records ---

    public record SafehouseListResult(List<SafehouseInfo> safehouses, List<String> warnings) {
    }

    public record ExportResult(int safehouseCount, int totalBinKeys, int binsWritten, long totalBytes,
                                List<String> warnings) {
    }
}
