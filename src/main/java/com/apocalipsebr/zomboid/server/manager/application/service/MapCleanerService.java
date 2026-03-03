package com.apocalipsebr.zomboid.server.manager.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MapCleanerService {

    private static final Logger log = LoggerFactory.getLogger(MapCleanerService.class);
    private static final int BIN_TILE_SIZE = 8;
    // Matches town strings like "Muldraugh, KY", "Raven Creek, RC", "Bedford Falls, BF", etc.
    private static final Pattern TOWN_RE = Pattern.compile(".+, [A-Za-z]{2,}$");

    @Value("${server.map.folder:}")
    private String mapFolderPath;

    // --- Public API ---

    /**
     * Scans the map folder and builds a complete index (bins + safehouses).
     * This replaces the Python helper script.
     */
    public MapIndex buildIndex() {
        if (mapFolderPath == null || mapFolderPath.isBlank()) {
            return MapIndex.error("server.map.folder is not configured.");
        }

        Path mapDir = Paths.get(mapFolderPath);
        if (!Files.exists(mapDir) || !Files.isDirectory(mapDir)) {
            return MapIndex.error("Map folder does not exist: " + mapFolderPath);
        }

        long t0 = System.currentTimeMillis();

        List<String> warnings = new ArrayList<>();
        ScanResult scan = scanMapBins(mapDir, warnings);
        long t1 = System.currentTimeMillis();

        Path saveRoot = mapDir.getParent();
        Path metaPath = saveRoot != null ? saveRoot.resolve("map_meta.bin") : mapDir.resolve("../map_meta.bin").normalize();
        List<SafehouseInfo> safehouses = extractSafehousesFromMapMeta(metaPath, warnings);
        long t2 = System.currentTimeMillis();

        return new MapIndex(
                scan.binsByX(),
                scan.totalBins(),
                BIN_TILE_SIZE,
                safehouses,
                warnings,
                (t1 - t0) / 1000.0,
                (t2 - t1) / 1000.0
        );
    }

    /**
     * Deletes the specified .bin files from the map folder.
     * Input: list of "bx/by" keys (folder/file).
     * Returns a result with count of deleted files and any errors.
     */
    public DeleteResult deleteBins(List<String> binKeys) {
        if (mapFolderPath == null || mapFolderPath.isBlank()) {
            return new DeleteResult(false, 0, 0, "server.map.folder is not configured.");
        }

        Path mapDir = Paths.get(mapFolderPath);
        if (!Files.exists(mapDir) || !Files.isDirectory(mapDir)) {
            return new DeleteResult(false, 0, 0, "Map folder does not exist: " + mapFolderPath);
        }

        int deleted = 0;
        int notFound = 0;
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

        String message = String.format("Deleted %d files (%d not found, %d errors).", deleted, notFound, errors.size());
        if (!errors.isEmpty()) {
            message += " Errors: " + String.join("; ", errors.subList(0, Math.min(10, errors.size())));
        }

        log.info("Map cleaner delete: {} files deleted, {} not found, {} errors out of {} requested",
                deleted, notFound, errors.size(), binKeys.size());

        return new DeleteResult(errors.isEmpty(), deleted, binKeys.size(), message);
    }

    /**
     * Returns the configured map folder path.
     */
    public String getMapFolderPath() {
        return mapFolderPath;
    }

    // --- Scanning ---

    private ScanResult scanMapBins(Path mapDir, List<String> warnings) {
        Map<String, List<int[]>> binsByX = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
        int total = 0;

        try (DirectoryStream<Path> folders = Files.newDirectoryStream(mapDir)) {
            for (Path folder : folders) {
                if (!Files.isDirectory(folder)) continue;

                String folderName = folder.getFileName().toString();
                if (!folderName.matches("\\d+")) continue;

                List<Integer> ys = new ArrayList<>();
                try (DirectoryStream<Path> files = Files.newDirectoryStream(folder, "*.bin")) {
                    for (Path file : files) {
                        String stem = file.getFileName().toString().replace(".bin", "");
                        if (stem.matches("\\d+")) {
                            ys.add(Integer.parseInt(stem));
                        }
                    }
                } catch (IOException e) {
                    warnings.add("Error reading folder " + folderName + ": " + e.getMessage());
                    continue;
                }

                if (!ys.isEmpty()) {
                    Collections.sort(ys);
                    List<int[]> ranges = compressToRanges(ys);
                    binsByX.put(folderName, ranges);
                    for (int[] r : ranges) {
                        total += (r[1] - r[0] + 1);
                    }
                }
            }
        } catch (IOException e) {
            warnings.add("Error scanning map directory: " + e.getMessage());
        }

        return new ScanResult(binsByX, total);
    }

    private List<int[]> compressToRanges(List<Integer> sorted) {
        if (sorted.isEmpty()) return Collections.emptyList();

        List<int[]> ranges = new ArrayList<>();
        int a = sorted.get(0), b = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            int v = sorted.get(i);
            if (v == b + 1) {
                b = v;
            } else {
                ranges.add(new int[]{a, b});
                a = b = v;
            }
        }
        ranges.add(new int[]{a, b});
        return ranges;
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

    // --- Safehouse extraction from map_meta.bin (port of Python logic) ---

    private List<SafehouseInfo> extractSafehousesFromMapMeta(Path metaPath, List<String> warnings) {
        if (!Files.exists(metaPath)) {
            warnings.add("map_meta.bin not found at " + metaPath);
            return Collections.emptyList();
        }

        byte[] data;
        try {
            data = Files.readAllBytes(metaPath);
        } catch (IOException e) {
            warnings.add("Failed to read map_meta.bin: " + e.getMessage());
            return Collections.emptyList();
        }

        // Scan for safehouse signature blocks: [int x][int y][int w][int h][UTF owner][long][UTF owner]
        List<SafehouseCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < data.length - 48; i++) {
            Integer x = readI32(data, i);
            Integer y = readI32(data, i + 4);
            Integer w = readI32(data, i + 8);
            Integer h = readI32(data, i + 12);

            if (x == null || y == null || w == null || h == null) break;
            if (x < 0 || x > 40000 || y < 0 || y > 40000) continue;
            if (w < 1 || w > 800 || h < 1 || h > 800) continue;

            StringRead t1 = readUTF(data, i + 16);
            if (t1 == null) continue;
            if (!isReasonableName(t1.value())) continue;

            if (t1.nextPos() + 8 + 2 > data.length) continue;
            StringRead t2 = readUTF(data, t1.nextPos() + 8);
            if (t2 == null) continue;
            if (!t1.value().equals(t2.value())) continue;

            candidates.add(new SafehouseCandidate(i, x, y, w, h, t1.value(), t2.nextPos()));
        }

        if (candidates.isEmpty()) {
            warnings.add("No safehouse signature blocks found in map_meta.bin.");
            return Collections.emptyList();
        }

        List<SafehouseInfo> records = new ArrayList<>();

        for (int idx = 0; idx < candidates.size(); idx++) {
            SafehouseCandidate c = candidates.get(idx);
            int nextOff = (idx + 1 < candidates.size())
                    ? candidates.get(idx + 1).offset()
                    : Math.min(data.length, c.windowStart() + 2500);

            int windowStart = c.windowStart();
            int windowEnd = Math.min(data.length, nextOff);

            List<StringAt> strings = scanUTFStrings(data, windowStart, windowEnd, 140);

            // Deduplicate while preserving order
            Set<String> seen = new LinkedHashSet<>();
            List<StringAt> ordered = new ArrayList<>();
            for (StringAt s : strings) {
                if (seen.add(s.value())) {
                    ordered.add(s);
                }
            }

            // Find town
            String town = "";
            int townPos = -1;
            for (StringAt s : ordered) {
                if (s.value().equals(c.owner())) continue;
                if (TOWN_RE.matcher(s.value()).matches()) {
                    town = s.value();
                    townPos = s.pos();
                    break;
                }
            }

            if (town.isEmpty()) {
                log.debug("Safehouse candidate at offset {} (owner={}, x={}, y={}, w={}, h={}) has no recognized town — keeping with empty town.",
                        c.offset(), c.owner(), c.x(), c.y(), c.w(), c.h());
            }

            // Capture effectively-final copies for use below
            final int fTownPos = townPos;
            final String fTown = town;
            final String fOwner = c.owner();

            // Find name (only when a town was found as positional anchor)
            String name = "";
            if (fTownPos >= 0) {
                // Check strings before town
                List<StringAt> before = ordered.stream()
                        .filter(s -> s.pos() < fTownPos && !s.value().equals(fOwner) && !s.value().equals(fTown))
                        .sorted(Comparator.comparingInt(s -> Math.abs(s.pos() - fTownPos)))
                        .toList();

                for (StringAt s : before) {
                    if (fTownPos - s.pos() <= 140) {
                        name = s.value();
                        break;
                    }
                }

                if (name.isEmpty()) {
                    List<StringAt> after = ordered.stream()
                            .filter(s -> s.pos() > fTownPos && !s.value().equals(fOwner) && !s.value().equals(fTown))
                            .sorted(Comparator.comparingInt(StringAt::pos))
                            .toList();

                    if (!after.isEmpty() && after.get(0).pos() - fTownPos <= 220) {
                        name = after.get(0).value();
                    }
                }
            }

            // Find members
            List<String> members = new ArrayList<>();
            Set<String> memberSeen = new HashSet<>();
            for (StringAt s : ordered) {
                if (s.value().equals(c.owner()) || s.value().equals(town) || s.value().equals(name)) continue;
                if (TOWN_RE.matcher(s.value()).matches()) continue;
                if (isReasonableName(s.value()) && memberSeen.add(s.value())) {
                    members.add(s.value());
                }
                if (townPos >= 0 && s.pos() > townPos + 300) break;
            }

            records.add(new SafehouseInfo(c.x(), c.y(), c.w(), c.h(), c.owner(), town, name, members));
        }

        if (records.isEmpty()) {
            warnings.add("Safehouse candidates found, but no records passed town-validation.");
        }

        return records;
    }

    // --- Binary helpers ---

    private Integer readI32(byte[] data, int offset) {
        if (offset + 4 > data.length) return null;
        return ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private Integer readU16(byte[] data, int offset) {
        if (offset + 2 > data.length) return null;
        return ByteBuffer.wrap(data, offset, 2).order(ByteOrder.BIG_ENDIAN).getShort() & 0xFFFF;
    }

    private StringRead readUTF(byte[] data, int offset) {
        Integer len = readU16(data, offset);
        if (len == null) return null;
        int start = offset + 2;
        if (start + len > data.length) return null;
        String value = new String(data, start, len, java.nio.charset.StandardCharsets.UTF_8);
        return new StringRead(value, start + len);
    }

    private boolean isReasonableName(String s) {
        if (s == null || s.length() < 1 || s.length() > 40) return false;
        for (char c : s.toCharArray()) {
            if (c < 32) return false;
        }
        // \p{L} supports Unicode letters (accented chars, Cyrillic, etc.)
        return s.matches("[\\p{L}\\p{N}_\\-\\.\\s]{1,40}") && !s.trim().isEmpty();
    }

    private List<StringAt> scanUTFStrings(byte[] data, int start, int end, int maxLen) {
        List<StringAt> out = new ArrayList<>();
        int i = start;
        while (i + 2 < end) {
            Integer len = readU16(data, i);
            if (len == null) break;
            if (len > 0 && len <= maxLen && i + 2 + len <= end) {
                byte[] raw = Arrays.copyOfRange(data, i + 2, i + 2 + len);
                int printable = 0;
                for (byte b : raw) {
                    if (b >= 32 && b < 127) printable++;
                }
                if ((double) printable / Math.max(1, len) > 0.85) {
                    String s = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
                    if (!s.trim().isEmpty()) {
                        boolean allPrintable = true;
                        for (char c : s.toCharArray()) {
                            if (c < 32) { allPrintable = false; break; }
                        }
                        if (allPrintable) {
                            out.add(new StringAt(i, s));
                        }
                    }
                }
            }
            i++;
        }
        return out;
    }

    // --- Records ---

    public record MapIndex(
            Map<String, List<int[]>> binsByX,
            int totalBins,
            int binTileSize,
            List<SafehouseInfo> safehouses,
            List<String> warnings,
            double scanSeconds,
            double safehouseSeconds
    ) {
        public static MapIndex error(String msg) {
            return new MapIndex(Collections.emptyMap(), 0, BIN_TILE_SIZE,
                    Collections.emptyList(), List.of(msg), 0, 0);
        }
    }

    public record SafehouseInfo(int x, int y, int w, int h, String owner, String town, String name,
                                List<String> members) {
    }

    public record DeleteResult(boolean success, int deletedCount, int requestedCount, String message) {
    }

    private record ScanResult(Map<String, List<int[]>> binsByX, int totalBins) {
    }

    private record SafehouseCandidate(int offset, int x, int y, int w, int h, String owner, int windowStart) {
    }

    private record StringRead(String value, int nextPos) {
    }

    private record StringAt(int pos, String value) {
    }
}
