package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService;
import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService.ClaimPreview;
import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService.CreateClaimResult;
import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService.ReviewClaimResult;
import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService.SafehouseListResult;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SafehouseClaimRequest;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class SafehouseController {

    private static final Logger log = LoggerFactory.getLogger(SafehouseController.class);

    private final SafehouseService safehouseService;
    private final CharacterService characterService;

    public SafehouseController(SafehouseService safehouseService, CharacterService characterService) {
        this.safehouseService = safehouseService;
        this.characterService = characterService;
    }

    @GetMapping("/admin/safehouses")
    @PreAuthorize("hasRole('ADMIN')")
    public String safehousesPage() {
        return "safehouses";
    }

    @GetMapping("/admin/safehouses/api/list")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listSafehouses() {
        SafehouseListResult result = safehouseService.listSafehouses();

        List<Map<String, Object>> safehousesJson = new ArrayList<>();
        for (var sh : result.safehouses()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x", sh.x());
            m.put("y", sh.y());
            m.put("w", sh.w());
            m.put("h", sh.h());
            m.put("owner", sh.owner());
            m.put("town", sh.town());
            m.put("name", sh.name());
            m.put("members", sh.members());
            safehousesJson.add(m);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("safehouses", safehousesJson);
        response.put("count", result.safehouses().size());
        response.put("warnings", result.warnings());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/safehouses/api/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> exportSafehouses(
            @RequestParam(defaultValue = "2") int margin) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        String filename = "safehouses-export-" + timestamp + ".zip";

        StreamingResponseBody body = outputStream -> {
            try {
                safehouseService.exportSafehouseBinsAsZip(margin, outputStream);
            } catch (IllegalStateException e) {
                log.error("Safehouse export failed: {}", e.getMessage());
                throw e;
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    @GetMapping("/safehouses/claim")
    public String claimSafehousePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("userBalance", characterService.getTotalCurrency(user));
        model.addAttribute("userCharacters", characterService.getUserCharacters(user));
        return "claim-safehouse";
    }

    @GetMapping("/safehouses/map-selector")
    public String claimMapSelectorPage(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "map-region-selector";
    }

    @GetMapping("/safehouses/api/safehouses")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getSafehousesForMap(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(401).body(null);

        SafehouseListResult result = safehouseService.listSafehouses();
        List<Map<String, Object>> list = new ArrayList<>();
        for (var sh : result.safehouses()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x", sh.x());
            m.put("y", sh.y());
            m.put("w", sh.w());
            m.put("h", sh.h());
            m.put("owner", sh.owner());
            m.put("town", sh.town());
            m.put("name", sh.name());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }


    @GetMapping("/safehouses/api/preview")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> previewClaim(
            @RequestParam int x1,
            @RequestParam int y1,
            @RequestParam int x2,
            @RequestParam int y2,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Não autenticado"));
        }

        ClaimPreview preview = safehouseService.previewClaim(x1, y1, x2, y2);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cost", preview.cost());
        response.put("baseCost", preview.baseCost());
        response.put("area", preview.area());
        response.put("balance", characterService.getTotalCurrency(user));
        response.put("overlapsExisting", preview.overlapsExisting());
        response.put("overlapCount", preview.overlapCount());
        response.put("overlappingSafehouses", preview.overlappingSafehouses());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/safehouses/api/claim")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createClaim(
            @RequestParam Long ownerCharacterId,
            @RequestParam int x1,
            @RequestParam int y1,
            @RequestParam int x2,
            @RequestParam int y2,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Não autenticado"));
        }

        CreateClaimResult result = safehouseService.requestClaim(user, ownerCharacterId, x1, y1, x2, y2);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        response.put("balance", characterService.getTotalCurrency(user));
        if (result.claim() != null) {
            response.put("claimId", result.claim().getId());
            response.put("status", result.claim().getStatus().name());
            response.put("cost", result.claim().getCost());
            response.put("ownerCharacter", result.claim().getClaimName());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/safehouses/api/my-claims")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> myClaims(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(null);
        }

        return ResponseEntity.ok(toClaimResponseList(safehouseService.getUserClaims(user)));
    }

    @PostMapping("/safehouses/api/claims/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelClaim(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Não autenticado"));
        }

        ReviewClaimResult result = safehouseService.cancelClaim(user, id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        response.put("balance", characterService.getTotalCurrency(user));
        if (result.claim() != null) {
            response.put("claim", toClaimResponse(result.claim()));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/safehouses/claims")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageClaimRequests(Model model) {
        List<SafehouseClaimRequest> pending = safehouseService.getPendingClaims();
        List<SafehouseClaimRequest> recent = safehouseService.getRecentClaims();
        model.addAttribute("pendingClaims", pending);
        model.addAttribute("recentClaims", recent);
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("totalClaims", recent.size());
        return "safehouse-claims-manage";
    }

    @GetMapping("/admin/safehouses/api/claims")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listClaimRequests() {
        return ResponseEntity.ok(toClaimResponseList(safehouseService.getRecentClaims()));
    }

    @PostMapping("/admin/safehouses/api/claims/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> approveClaim(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");
        ReviewClaimResult result = safehouseService.approveClaim(id, admin != null ? admin.getUsername() : "admin", reason);
        return ResponseEntity.ok(reviewResponse(result));
    }

    @PostMapping("/admin/safehouses/api/claims/{id}/deny")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> denyClaim(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");
        ReviewClaimResult result = safehouseService.denyClaim(id, admin != null ? admin.getUsername() : "admin", reason);
        return ResponseEntity.ok(reviewResponse(result));
    }

    private Map<String, Object> reviewResponse(ReviewClaimResult result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.success());
        response.put("message", result.message());
        if (result.claim() != null) {
            response.put("claim", toClaimResponse(result.claim()));
        }
        return response;
    }

    private List<Map<String, Object>> toClaimResponseList(List<SafehouseClaimRequest> claims) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SafehouseClaimRequest claim : claims) {
            result.add(toClaimResponse(claim));
        }
        return result;
    }

    private Map<String, Object> toClaimResponse(SafehouseClaimRequest claim) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", claim.getId());
        map.put("claimName", claim.getClaimName());
        map.put("ownerCharacter", claim.getClaimName());
        map.put("status", claim.getStatus().name());
        map.put("statusDisplay", claim.getStatus().getDisplayName());
        map.put("username", claim.getUser().getUsername());
        map.put("userId", claim.getUser().getId());
        map.put("x1", claim.getX1());
        map.put("y1", claim.getY1());
        map.put("x2", claim.getX2());
        map.put("y2", claim.getY2());
        map.put("cost", claim.getCost());
        map.put("area", claim.getArea());
        map.put("overlapsExisting", claim.getOverlapsExisting());
        map.put("overlapCount", claim.getOverlapCount());
        map.put("adminReason", claim.getAdminReason());
        map.put("reviewedBy", claim.getReviewedBy());
        map.put("reviewedAt", claim.getReviewedAt() != null ? claim.getReviewedAt().toString() : null);
        map.put("createdAt", claim.getCreatedAt() != null ? claim.getCreatedAt().toString() : null);
        return map;
    }
}
