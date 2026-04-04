package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.MapCleanerService;
import com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.MapIndex;
import com.apocalipsebr.zomboid.server.manager.application.service.SoftWipeService;
import com.apocalipsebr.zomboid.server.manager.application.service.SoftWipeService.CreateSoftWipeResult;
import com.apocalipsebr.zomboid.server.manager.application.service.SoftWipeService.CancelResult;
import com.apocalipsebr.zomboid.server.manager.application.service.SoftWipeService.SoftWipePreview;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Region;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SoftWipe;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.RegionRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/soft-wipe")
public class SoftWipeController {

    private final SoftWipeService softWipeService;
    private final CharacterService characterService;
    private final MapCleanerService mapCleanerService;
    private final RegionRepository regionRepository;
    private final ClaimedCarRepository claimedCarRepository;

    public SoftWipeController(SoftWipeService softWipeService,
                              CharacterService characterService,
                              MapCleanerService mapCleanerService,
                              RegionRepository regionRepository,
                              ClaimedCarRepository claimedCarRepository) {
        this.softWipeService = softWipeService;
        this.characterService = characterService;
        this.mapCleanerService = mapCleanerService;
        this.regionRepository = regionRepository;
        this.claimedCarRepository = claimedCarRepository;
    }

    // ==================== PAGE ====================

    @GetMapping
    public String softWipePage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        return "soft-wipe";
    }

    // ==================== PREVIEW ====================

    /**
     * Returns a cost/bin preview for the given rectangle.
     */
    @GetMapping("/api/preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> preview(
            @RequestParam int x1, @RequestParam int y1,
            @RequestParam int x2, @RequestParam int y2,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        SoftWipePreview preview = softWipeService.getPreview(x1, y1, x2, y2);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalBins", preview.totalBins());
        response.put("cost", preview.cost());
        response.put("area", preview.area());
        response.put("balance", characterService.getTotalCurrency(user));
        response.put("enabled", softWipeService.isSoftWipeEnabled());
        response.put("minCost", softWipeService.getMinCost());
        response.put("areaCostFactor", softWipeService.getAreaCostFactor());

        return ResponseEntity.ok(response);
    }

    // ==================== REQUEST ====================

    /**
     * Creates a soft-wipe request. Deducts currency and queues the wipe
     * for execution at the next server restart.
     */
    @PostMapping("/api/request")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> requestWipe(
            @RequestBody SoftWipeRequest request,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        CreateSoftWipeResult result = softWipeService.requestSoftWipe(
                user, request.x1(), request.y1(), request.x2(), request.y2());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        if (result.wipe() != null) {
            response.put("wipeId", result.wipe().getId());
            response.put("status", result.wipe().getStatus().name());
            response.put("cost", result.wipe().getCost());
        }
        response.put("balance", characterService.getTotalCurrency(user));

        return ResponseEntity.ok(response);
    }

    // ==================== CANCEL ====================

    @PostMapping("/api/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelWipe(
            @PathVariable Long id,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        CancelResult result = softWipeService.cancelSoftWipe(id, user);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        response.put("balance", characterService.getTotalCurrency(user));

        return ResponseEntity.ok(response);
    }

    // ==================== MY WIPES ====================

    @GetMapping("/api/my-wipes")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> myWipes(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(null);

        List<SoftWipe> wipes = softWipeService.getUserWipes(user);
        List<Map<String, Object>> result = new ArrayList<>();

        for (SoftWipe w : wipes) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", w.getId());
            m.put("status", w.getStatus().name());
            m.put("statusDisplay", w.getStatus().getDisplayName());
            m.put("x1", w.getX1());
            m.put("y1", w.getY1());
            m.put("x2", w.getX2());
            m.put("y2", w.getY2());
            m.put("cost", w.getCost());
            m.put("area", w.getArea());
            m.put("createdAt", w.getCreatedAt() != null ? w.getCreatedAt().toString() : null);
            m.put("executedAt", w.getExecutedAt() != null ? w.getExecutedAt().toString() : null);
            m.put("binsDeleted", w.getBinsDeleted());
            m.put("binsProtected", w.getBinsProtected());
            m.put("errorMessage", w.getErrorMessage());
            result.add(m);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== BALANCE ====================

    @GetMapping("/api/balance")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> balance(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));

        return ResponseEntity.ok(Map.of("balance", characterService.getTotalCurrency(user)));
    }

    // ==================== MAP DATA FOR OVERLAYS ====================

    /**
     * Returns safehouses from map_meta.bin for rendering protection overlays.
     */
    @GetMapping("/api/safehouses")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSafehouses(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(null);

        try {
            MapIndex index = mapCleanerService.buildIndex();
            List<Map<String, Object>> result = new ArrayList<>();

            for (var sh : index.safehouses()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("x", sh.x());
                m.put("y", sh.y());
                m.put("w", sh.w());
                m.put("h", sh.h());
                m.put("owner", sh.owner());
                m.put("town", sh.town());
                m.put("name", sh.name());
                result.add(m);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * Returns claimed cars with coordinates for rendering protection overlays.
     */
    @GetMapping("/api/claimed-cars")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getClaimedCars(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(null);

        List<ClaimedCar> cars = claimedCarRepository.findByXNotNullAndYNotNull();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClaimedCar car : cars) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("ownerName", car.getOwnerName());
            m.put("vehicleName", car.getVehicleName());
            m.put("x", car.getX());
            m.put("y", car.getY());
            result.add(m);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Returns enabled regions for rendering on the map overlay.
     */
    @GetMapping("/api/regions")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRegions(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(null);

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
            m.put("permanent", r.getPermanent());
            result.add(m);
        }

        return ResponseEntity.ok(result);
    }

    // ==================== DTOs ====================

    public record SoftWipeRequest(int x1, int y1, int x2, int y2) {}
}
