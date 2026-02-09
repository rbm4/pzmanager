package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.RegionService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Region;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/regions")
public class RegionWebController {

    private final RegionService regionService;

    public RegionWebController(RegionService regionService) {
        this.regionService = regionService;
    }

    // ==================== Admin Management ====================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/manage")
    public String manageRegions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Region> regionsPage = regionService.getRegionsPaginated(search, false, category, pageable);

        model.addAttribute("regions", regionsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", regionsPage.getTotalPages());
        model.addAttribute("totalRegions", regionService.getTotalRegionCount());
        model.addAttribute("enabledCount", regionService.getEnabledRegionCount());
        model.addAttribute("search", search);
        model.addAttribute("category", category);

        return "regions-manage";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("region", new Region());
        return "region-create";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String createRegion(
            @ModelAttribute Region region,
            @RequestParam(value = "propNames", required = false) List<String> propNames,
            @RequestParam(value = "propValues", required = false) List<String> propValues,
            RedirectAttributes redirectAttributes) {
        try {
            if (region.getEnabled() == null) { region.setEnabled(false); }
            if (region.getPermanent() == null) { region.setPermanent(false); }
            regionService.createRegion(region, propNames, propValues);
            redirectAttributes.addAttribute("success", "created");
            return "redirect:/regions/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falha ao criar região: " + e.getMessage());
            return "redirect:/regions/create";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return regionService.getRegionById(id)
                .map(region -> {
                    model.addAttribute("region", region);
                    return "region-edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addAttribute("error", "notfound");
                    return "redirect:/regions/manage";
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateRegion(
            @PathVariable Long id,
            @ModelAttribute Region region,
            @RequestParam(value = "propNames", required = false) List<String> propNames,
            @RequestParam(value = "propValues", required = false) List<String> propValues,
            RedirectAttributes redirectAttributes) {
        try {
            if (region.getEnabled() == null) { region.setEnabled(false); }
            if (region.getPermanent() == null) { region.setPermanent(false); }
            regionService.updateRegion(id, region, propNames, propValues);
            redirectAttributes.addAttribute("success", "updated");
            return "redirect:/regions/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Falha ao atualizar região: " + e.getMessage());
            return "redirect:/regions/" + id + "/edit";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            regionService.toggleEnabled(id);
            redirectAttributes.addAttribute("success", "toggled");
            return "redirect:/regions/manage";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "failed");
            return "redirect:/regions/manage";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteRegion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            regionService.deleteRegion(id);
            redirectAttributes.addAttribute("success", "deleted");
            return "redirect:/regions/manage";
        } catch (IllegalStateException e) {
            redirectAttributes.addAttribute("error", "permanent");
            return "redirect:/regions/manage";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "failed");
            return "redirect:/regions/manage";
        }
    }

    // ==================== JSON Export ====================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/export")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> exportRegionsJson() {
        try {
            regionService.writeRegionsJsonToFile();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Regions JSON successfully written to file for mod consumption"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "error", "Failed to write regions file: " + e.getMessage()
                ));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/preview-json")
    @ResponseBody
    public ResponseEntity<String> previewRegionsJson() {
        String json = regionService.generateRegionsJson();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }
}
