package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Region;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.RegionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Public controller for the read-only map regions viewer.
 * No authentication required — intended for the season-guide page.
 */
@Controller
@RequestMapping("/map-regions")
public class MapRegionsController {

    private final RegionRepository regionRepository;

    public MapRegionsController(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @GetMapping
    public String mapRegionsPage() {
        return "map-regions";
    }

    @GetMapping("/viewer")
    public String mapRegionViewerPage() {
        return "map-region-viewer";
    }

    @GetMapping("/api/regions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRegions() {
        List<Region> regions = regionRepository.findByEnabledTrue();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Region r : regions) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("code", r.getCode());
            m.put("name", r.getName());
            m.put("categories", r.getCategories());
            m.put("x1", r.getX1());
            m.put("y1", r.getY1());
            m.put("x2", r.getX2());
            m.put("y2", r.getY2());
            m.put("z", r.getZ());
            m.put("permanent", r.getPermanent());

            // Include custom properties with numeric value > 0
            Map<String, Object> activeProps = new LinkedHashMap<>();
            if (r.getCustomProperties() != null) {
                for (var prop : r.getCustomProperties()) {
                    String val = prop.getValue();
                    if (val == null || val.isBlank()) continue;
                    try {
                        double numVal = Double.parseDouble(val);
                        if (numVal > 0) {
                            // Use int if it's a whole number, otherwise double
                            if (numVal == Math.floor(numVal) && !Double.isInfinite(numVal)) {
                                activeProps.put(prop.getName(), (int) numVal);
                            } else {
                                activeProps.put(prop.getName(), numVal);
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Non-numeric values (e.g. "true") — include as-is if not "0"/"false"
                        if (!"0".equals(val) && !"false".equalsIgnoreCase(val)) {
                            activeProps.put(prop.getName(), val);
                        }
                    }
                }
            }
            if (!activeProps.isEmpty()) {
                m.put("properties", activeProps);
            }

            result.add(m);
        }

        return ResponseEntity.ok(result);
    }
}
