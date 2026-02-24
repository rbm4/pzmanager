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
            result.add(m);
        }

        return ResponseEntity.ok(result);
    }
}
