package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.MapCleanerService;
import com.apocalipsebr.zomboid.server.manager.application.service.MapCleanerService.DeleteResult;
import com.apocalipsebr.zomboid.server.manager.application.service.MapCleanerService.MapIndex;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/map-cleaner")
public class MapCleanerController {

    private final MapCleanerService mapCleanerService;

    public MapCleanerController(MapCleanerService mapCleanerService) {
        this.mapCleanerService = mapCleanerService;
    }

    /**
     * Serves the map cleaner page.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String mapCleanerPage() {
        return "map-cleaner";
    }

    /**
     * Scans the server map folder and returns the index JSON.
     * Replaces the Python helper script — the server scans the disk directly.
     */
    @GetMapping("/api/index")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMapIndex() {
        MapIndex index = mapCleanerService.buildIndex();

        // Convert bins_by_x to the same format the JS expects
        Map<String, List<List<Integer>>> binsForJson = new LinkedHashMap<>();
        for (var entry : index.binsByX().entrySet()) {
            List<List<Integer>> ranges = new ArrayList<>();
            for (int[] r : entry.getValue()) {
                ranges.add(List.of(r[0], r[1]));
            }
            binsForJson.put(entry.getKey(), ranges);
        }

        // Convert safehouses to maps
        List<Map<String, Object>> safehousesForJson = new ArrayList<>();
        for (var sh : index.safehouses()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x", sh.x());
            m.put("y", sh.y());
            m.put("w", sh.w());
            m.put("h", sh.h());
            m.put("owner", sh.owner());
            m.put("town", sh.town());
            m.put("name", sh.name());
            m.put("members", sh.members());
            safehousesForJson.add(m);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("version", 1);
        response.put("bin_tile_size", index.binTileSize());
        response.put("bins", index.totalBins());
        response.put("bins_by_x", binsForJson);
        response.put("safehouses", safehousesForJson);
        response.put("warnings", index.warnings());
        response.put("timing_seconds", Map.of(
                "scan_map_bins", index.scanSeconds(),
                "extract_safehouses", index.safehouseSeconds(),
                "total", index.scanSeconds() + index.safehouseSeconds()
        ));
        response.put("map_folder", mapCleanerService.getMapFolderPath());

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the specified .bin files from the map folder.
     * Expects JSON body: { "bins": ["1250/625", "1250/626", ...] }
     */
    @PostMapping("/api/delete")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteBins(@RequestBody DeleteRequest request) {
        if (request.bins() == null || request.bins().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "No bins specified for deletion."
            ));
        }

        DeleteResult result = mapCleanerService.deleteBins(request.bins());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.success());
        response.put("deletedCount", result.deletedCount());
        response.put("requestedCount", result.requestedCount());
        response.put("message", result.message());

        return ResponseEntity.ok(response);
    }

    /**
     * Returns basic info about the map folder configuration.
     */
    @GetMapping("/api/status")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStatus() {
        String path = mapCleanerService.getMapFolderPath();
        boolean configured = path != null && !path.isBlank();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("configured", configured);
        response.put("mapFolder", path != null ? path : "");

        return ResponseEntity.ok(response);
    }

    public record DeleteRequest(List<String> bins) {
    }
}
