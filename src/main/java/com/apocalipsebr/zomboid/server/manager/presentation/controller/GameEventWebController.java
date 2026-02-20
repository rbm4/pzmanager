package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.constants.EventPropertySuggestion;
import com.apocalipsebr.zomboid.server.manager.application.service.GameEventService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEvent;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEvent.EventStatus;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEventProperty;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/events")
public class GameEventWebController {

    private final GameEventService gameEventService;

    public GameEventWebController(GameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    // ==================== LIST (Cards View) ====================

    @GetMapping
    public String listEvents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        EventStatus filterStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                filterStatus = EventStatus.valueOf(status);
            } catch (IllegalArgumentException ignored) {}
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<GameEvent> eventsPage = gameEventService.getEventsPaginated(search, filterStatus, pageable);

        model.addAttribute("events", eventsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalEvents", gameEventService.getTotalEventCount());
        model.addAttribute("activeCount", gameEventService.countByStatus(EventStatus.ACTIVE));
        model.addAttribute("pendingCount", gameEventService.countByStatus(EventStatus.PENDING));
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("statuses", EventStatus.values());
        model.addAttribute("userBalance", gameEventService.getUserBalance(user));
        model.addAttribute("currentUserId", user.getId());

        return "events-list";
    }

    // ==================== CREATE FORM ====================

    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Filter sandbox suggestions based on current sandbox values (maxValue validation)
        GameEventService.FilteredSandboxSuggestions filtered = gameEventService.getFilteredSandboxSuggestions();

        model.addAttribute("sandboxSuggestions", filtered.suggestions());
        model.addAttribute("disabledTiersMap", filtered.disabledTiersMap());
        model.addAttribute("regionSuggestions", EventPropertySuggestion.getRegionSuggestions());
        model.addAttribute("percentageTiers", EventPropertySuggestion.PERCENTAGE_TIERS);
        model.addAttribute("percentageCostMultipliers", EventPropertySuggestion.PERCENTAGE_COST_MULTIPLIERS);
        model.addAttribute("userBalance", gameEventService.getUserBalance(user));
        model.addAttribute("weeklyEventsRemaining", gameEventService.getWeeklyEventsRemaining(user));

        return "event-create";
    }

    // ==================== CREATE (POST) ====================

    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createEvent(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(value = "suggestionKeys") List<String> suggestionKeys,
            @RequestParam(value = "selectedValues") List<String> selectedValues,
            @RequestParam(required = false) Integer regionX1,
            @RequestParam(required = false) Integer regionX2,
            @RequestParam(required = false) Integer regionY1,
            @RequestParam(required = false) Integer regionY2,
            @RequestParam(required = false) Integer regionZ,
            @RequestParam(required = false) String regionName,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "Sessão expirada. Faça login novamente.");
        }

        GameEventService.CreateEventResult result = gameEventService.createEvent(
            title, description, user, suggestionKeys, selectedValues,
            regionX1, regionX2, regionY1, regionY2, regionZ, regionName
        );

        if (result.success() && result.event() != null) {
            return Map.of("success", true, "message", result.message(), "eventId", result.event().getId());
        }
        return Map.of("success", result.success(), "message", result.message());
    }

    // ==================== CONTRIBUTE ====================

    @PostMapping("/{id}/contribute")
    @ResponseBody
    public Map<String, Object> contribute(
            @PathVariable Long id,
            @RequestParam Integer amount,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "Sessão expirada. Faça login novamente.");
        }

        GameEventService.ContributeResult result = gameEventService.contribute(id, amount, user);
        return Map.of(
            "success", result.success(),
            "message", result.message(),
            "activated", result.activated()
        );
    }

    // ==================== INSTANT ACTIVATE (pay full) ====================

    @PostMapping("/{id}/activate")
    @ResponseBody
    public Map<String, Object> instantActivate(
            @PathVariable Long id,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "Sessão expirada. Faça login novamente.");
        }

        return gameEventService.getEventById(id)
            .map(event -> {
                if (event.getStatus() != EventStatus.PENDING) {
                    return Map.<String, Object>of("success", false, "message", "Evento não pode ser ativado");
                }
                int remaining = event.getRemainingAmount();
                GameEventService.ContributeResult result = gameEventService.contribute(id, remaining, user);
                return Map.<String, Object>of(
                    "success", result.success(),
                    "message", result.message(),
                    "activated", result.activated()
                );
            })
            .orElse(Map.of("success", false, "message", "Evento não encontrado"));
    }

    // ==================== CANCEL EVENT ====================

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public Map<String, Object> cancelEvent(
            @PathVariable Long id,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "Sessão expirada. Faça login novamente.");
        }

        GameEventService.CancelEventResult result = gameEventService.cancelEvent(id, user);
        return Map.of("success", result.success(), "message", result.message());
    }

    // ==================== EVENT DETAIL (API) ====================

    @GetMapping("/{id}/detail")
    @ResponseBody
    public Map<String, Object> getEventDetail(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "Sessão expirada");
        }

        return gameEventService.getEventById(id)
            .map(event -> {
                Map<String, Object> data = new java.util.HashMap<>();
                data.put("success", true);
                data.put("id", event.getId());
                data.put("title", event.getTitle());
                data.put("description", event.getDescription());
                data.put("status", event.getStatus().name());
                data.put("totalCost", event.getTotalCost());
                data.put("amountCollected", event.getAmountCollected());
                data.put("remaining", event.getRemainingAmount());
                data.put("fundingPercentage", event.getFundingPercentage());
                data.put("createdBy", event.getCreatedBy().getUsername());
                data.put("createdByUserId", event.getCreatedBy().getId());
                data.put("createdAt", event.getCreatedAt().toString());

                // Always include expiration info
                if (event.getExpirationDate() != null) {
                    data.put("expirationDate", event.getExpirationDate().toString());
                } else {
                    // For pending events, show projected expiration
                    data.put("expirationDate", null);
                    data.put("durationDays", event.getDurationDays());
                }

                List<Map<String, Object>> props = event.getProperties().stream()
                    .map(p -> {
                        Map<String, Object> pm = new java.util.HashMap<>();
                        pm.put("displayName", p.getDisplayName());
                        pm.put("selectedValue", p.getSelectedValue());
                        pm.put("valueType", p.getValueType());
                        pm.put("propertyTarget", p.getPropertyTarget());
                        pm.put("cost", p.getPropertyCost());
                        if ("REGION".equals(p.getPropertyTarget()) && p.getRegionX1() != null) {
                            pm.put("regionX1", p.getRegionX1());
                            pm.put("regionY1", p.getRegionY1());
                            pm.put("regionX2", p.getRegionX2());
                            pm.put("regionY2", p.getRegionY2());
                        }
                        return pm;
                    }).toList();
                data.put("properties", props);

                // Add region zone summary if event has region properties
                boolean hasRegion = event.getProperties().stream()
                    .anyMatch(p -> "REGION".equals(p.getPropertyTarget()) && p.getRegionX1() != null);
                if (hasRegion) {
                    GameEventProperty regionProp = event.getProperties().stream()
                        .filter(p -> "REGION".equals(p.getPropertyTarget()) && p.getRegionX1() != null)
                        .findFirst().orElse(null);
                    if (regionProp != null) {
                        Map<String, Object> regionInfo = new java.util.HashMap<>();
                        regionInfo.put("x1", regionProp.getRegionX1());
                        regionInfo.put("y1", regionProp.getRegionY1());
                        regionInfo.put("x2", regionProp.getRegionX2());
                        regionInfo.put("y2", regionProp.getRegionY2());
                        data.put("regionZone", regionInfo);
                    }
                }

                List<Map<String, Object>> contribs = event.getContributions().stream()
                    .map(c -> {
                        Map<String, Object> cm = new java.util.HashMap<>();
                        cm.put("username", c.getUser().getUsername());
                        cm.put("amount", c.getAmount());
                        cm.put("date", c.getContributedAt().toString());
                        return cm;
                    }).toList();
                data.put("contributions", contribs);

                return data;
            })
            .orElse(Map.of("success", false, "message", "Evento não encontrado"));
    }
}
