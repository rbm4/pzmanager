package com.apocalipsebr.zomboid.server.manager.application.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MapDataService {

    private static final Logger log = LoggerFactory.getLogger(MapDataService.class);
    public static final int BIN_TILE_SIZE = 8;
    // Matches town strings like "Muldraugh, KY", "Raven Creek, RC", "Bedford Falls,
    // BF", etc.
    private static final Pattern TOWN_RE = Pattern.compile(".+, [A-Za-z]{2,}$");
    private static final int MIN_SAFEHOUSE_COORD = -200000;
    private static final int MAX_SAFEHOUSE_COORD = 200000;

    @Value("${server.map.folder:}")
    private String mapFolderPath;

    // --- Public API ---

    public String getMapFolderPath() {
        return mapFolderPath;
    }

    public Path getMapDir() {
        if (mapFolderPath == null || mapFolderPath.isBlank())
            return null;
        Path mapDir = Paths.get(mapFolderPath);
        if (!Files.exists(mapDir) || !Files.isDirectory(mapDir))
            return null;
        return mapDir;
    }

    public Path getMetaPath() {
        Path mapDir = getMapDir();
        if (mapDir == null)
            return null;
        Path saveRoot = mapDir.getParent();
        return saveRoot != null ? saveRoot.resolve("map_meta.bin") : mapDir.resolve("../map_meta.bin").normalize();
    }

    public List<SafehouseInfo> getSafehouses(List<String> warnings) {
        Path metaPath = getMetaPath();
        if (metaPath == null) {
            warnings.add("server.map.folder is not configured or does not exist.");
            return Collections.emptyList();
        }
        return extractSafehousesFromMapMeta(metaPath, warnings);
    }

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

        Path metaPath = getMetaPath();
        List<SafehouseInfo> safehouses = (metaPath != null)
                ? extractSafehousesFromMapMeta(metaPath, warnings)
                : Collections.emptyList();
        long t2 = System.currentTimeMillis();

        return new MapIndex(
                scan.binsByX(),
                scan.totalBins(),
                BIN_TILE_SIZE,
                safehouses,
                warnings,
                (t1 - t0) / 1000.0,
                (t2 - t1) / 1000.0);
    }

    // --- Scanning ---

    public ScanResult scanMapBins(Path mapDir, List<String> warnings) {
        Map<String, List<int[]>> binsByX = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));
        int total = 0;

        try (DirectoryStream<Path> folders = Files.newDirectoryStream(mapDir)) {
            for (Path folder : folders) {
                if (!Files.isDirectory(folder))
                    continue;

                String folderName = folder.getFileName().toString();
                if (!folderName.matches("\\d+"))
                    continue;

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

    public List<int[]> compressToRanges(List<Integer> sorted) {
        if (sorted.isEmpty())
            return Collections.emptyList();

        List<int[]> ranges = new ArrayList<>();
        int a = sorted.get(0), b = sorted.get(0);

        for (int i = 1; i < sorted.size(); i++) {
            int v = sorted.get(i);
            if (v == b + 1) {
                b = v;
            } else {
                ranges.add(new int[] { a, b });
                a = b = v;
            }
        }
        ranges.add(new int[] { a, b });
        return ranges;
    }

    // --- Safehouse extraction from map_meta.bin (port of Python logic) ---

    public List<SafehouseInfo> extractSafehousesFromMapMeta(Path metaPath, List<String> warnings) {
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

        List<SafehouseCandidate> candidates = new ArrayList<>();
        Set<Integer> foundOffsets = new HashSet<>();

        for (int i = 0; i < data.length - 48; i++) {
            Integer x = readI32(data, i);
            Integer y = readI32(data, i + 4);
            Integer w = readI32(data, i + 8);
            Integer h = readI32(data, i + 12);

            if (x == null || y == null || w == null || h == null)
                break;
            if (x < MIN_SAFEHOUSE_COORD || x > MAX_SAFEHOUSE_COORD || y < MIN_SAFEHOUSE_COORD
                    || y > MAX_SAFEHOUSE_COORD)
                continue;
            if (w < 1 || w > 800 || h < 1 || h > 800)
                continue;

            StringRead t1 = readUTF(data, i + 16);
            if (t1 == null)
                continue;
            if (!isReasonableName(t1.value()))
                continue;

            if (foundOffsets.add(i)) {
                candidates.add(new SafehouseCandidate(i, x, y, w, h, t1.value(), t1.nextPos()));
                log.debug("Safehouse candidate found at offset {} (owner={}, x={}, y={}, w={}, h={})",
                        i, t1.value(), x, y, w, h);
            }
        }

        if (candidates.isEmpty()) {
            warnings.add("No safehouse signature blocks found in map_meta.bin.");
            return Collections.emptyList();
        }

        candidates.sort(Comparator.comparingInt(SafehouseCandidate::offset));

        List<SafehouseInfo> records = new ArrayList<>();
        Set<String> uniqueSafehouseKeys = new HashSet<>();

        for (int idx = 0; idx < candidates.size(); idx++) {
            SafehouseCandidate c = candidates.get(idx);
            int nextOff = (idx + 1 < candidates.size())
                    ? candidates.get(idx + 1).offset()
                    : Math.min(data.length, c.windowStart() + 2500);

            int windowStart = c.windowStart();
            int windowEnd = Math.min(data.length, nextOff);

            List<StringAt> strings = scanUTFStrings(data, windowStart, windowEnd, 140);

            Set<String> seen = new LinkedHashSet<>();
            List<StringAt> ordered = new ArrayList<>();
            for (StringAt s : strings) {
                if (seen.add(s.value())) {
                    ordered.add(s);
                }
            }

            String town = "";
            int townPos = -1;
            for (StringAt s : ordered) {
                if (s.value().equals(c.owner()))
                    continue;
                if (TOWN_RE.matcher(s.value()).matches()) {
                    town = s.value();
                    townPos = s.pos();
                    break;
                }
            }

            if (town.isEmpty()) {
                log.debug(
                        "Safehouse candidate at offset {} (owner={}, x={}, y={}, w={}, h={}) has no recognized town — keeping with empty town.",
                        c.offset(), c.owner(), c.x(), c.y(), c.w(), c.h());
            }

            final int fTownPos = townPos;
            final String fTown = town;
            final String fOwner = c.owner();

            String name = "";
            if (fTownPos >= 0) {
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

            List<String> members = new ArrayList<>();
            Set<String> memberSeen = new HashSet<>();
            for (StringAt s : ordered) {
                if (s.value().equals(c.owner()) || s.value().equals(town) || s.value().equals(name))
                    continue;
                if (TOWN_RE.matcher(s.value()).matches())
                    continue;
                if (isReasonableName(s.value()) && memberSeen.add(s.value())) {
                    members.add(s.value());
                }
                if (townPos >= 0 && s.pos() > townPos + 300)
                    break;
            }

            String uniqueKey = c.x() + ":" + c.y() + ":" + c.w() + ":" + c.h() + ":" + c.owner();
            if (uniqueSafehouseKeys.add(uniqueKey)) {
                records.add(new SafehouseInfo(c.x(), c.y(), c.w(), c.h(), c.owner(), town, name, members));
            }
        }

        if (records.isEmpty()) {
            warnings.add("Safehouse candidates found, but no records passed town-validation.");
        }

        return records;
    }

    // --- Binary helpers ---

    private Integer readI32(byte[] data, int offset) {
        if (offset + 4 > data.length)
            return null;
        return ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    private Integer readU16(byte[] data, int offset) {
        if (offset + 2 > data.length)
            return null;
        return ByteBuffer.wrap(data, offset, 2).order(ByteOrder.BIG_ENDIAN).getShort() & 0xFFFF;
    }

    private StringRead readUTF(byte[] data, int offset) {
        Integer len = readU16(data, offset);
        if (len == null)
            return null;
        int start = offset + 2;
        if (start + len > data.length)
            return null;
        String value = new String(data, start, len, java.nio.charset.StandardCharsets.UTF_8);
        return new StringRead(value, start + len);
    }

    private boolean isReasonableName(String s) {
        if (s == null || s.length() < 1 || s.length() > 40)
            return false;
        if (s.equals("AnimalZone"))
            return false;
        for (char c : s.toCharArray()) {
            if (c < 32)
                return false;
        }
        return s.matches("[\\p{L}\\p{N}_\\-\\.\\s]{1,40}") && !s.trim().isEmpty();
    }

    private List<StringAt> scanUTFStrings(byte[] data, int start, int end, int maxLen) {
        List<StringAt> out = new ArrayList<>();
        int i = start;
        while (i + 2 < end) {
            Integer len = readU16(data, i);
            if (len == null)
                break;
            if (len > 0 && len <= maxLen && i + 2 + len <= end) {
                byte[] raw = Arrays.copyOfRange(data, i + 2, i + 2 + len);
                int printable = 0;
                for (byte b : raw) {
                    if (b >= 32 && b < 127)
                        printable++;
                }
                if ((double) printable / Math.max(1, len) > 0.85) {
                    String s = new String(raw, java.nio.charset.StandardCharsets.UTF_8);
                    if (!s.trim().isEmpty()) {
                        boolean allPrintable = true;
                        for (char c : s.toCharArray()) {
                            if (c < 32) {
                                allPrintable = false;
                                break;
                            }
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

    /** Integer ceiling division (works correctly for negative dividends). */
    public static int ceilDiv(int a, int b) {
        return Math.floorDiv(a + b - 1, b);
    }

    // --- Records ---

    public record MapIndex(
            Map<String, List<int[]>> binsByX,
            int totalBins,
            int binTileSize,
            List<SafehouseInfo> safehouses,
            List<String> warnings,
            double scanSeconds,
            double safehouseSeconds) {
        public static MapIndex error(String msg) {
            return new MapIndex(Collections.emptyMap(), 0, BIN_TILE_SIZE,
                    Collections.emptyList(), List.of(msg), 0, 0);
        }
    }

    public record SafehouseInfo(int x, int y, int w, int h, String owner, String town, String name,
            List<String> members) {
    }

    public record ScanResult(Map<String, List<int[]>> binsByX, int totalBins) {
    }

    private record SafehouseCandidate(int offset, int x, int y, int w, int h, String owner, int windowStart) {
    }

    private record StringRead(String value, int nextPos) {
    }

    private record StringAt(int pos, String value) {
    }

}
