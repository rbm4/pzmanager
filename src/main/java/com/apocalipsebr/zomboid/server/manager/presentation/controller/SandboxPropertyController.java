package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.SandboxPropertyService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting.ConfigType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controller for managing Zomboid Sandbox Settings.
 * Uses SandboxSetting entity backed by parsed servertest.ini data.
 */
@Controller
@RequestMapping("/admin/sandbox")
@PreAuthorize("hasRole('ADMIN')")
public class SandboxPropertyController {

    private final SandboxPropertyService sandboxPropertyService;

    public SandboxPropertyController(SandboxPropertyService sandboxPropertyService) {
        this.sandboxPropertyService = sandboxPropertyService;
    }

    // ==================== THYMELEAF VIEW ====================

    /**
     * Display the sandbox settings management page (paginated).
     */
    @GetMapping
    public String showSandboxManagement(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "25") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "configType", required = false) String configTypeStr,
            @RequestParam(value = "overwrite", required = false) String overwriteStr,
            Model model) {
        try {
            ConfigType configType = null;
            if (configTypeStr != null && !configTypeStr.isEmpty()) {
                try {
                    configType = ConfigType.valueOf(configTypeStr);
                } catch (IllegalArgumentException ignored) {}
            }

            Boolean overwrite = null;
            if (overwriteStr != null && !overwriteStr.isEmpty()) {
                overwrite = Boolean.valueOf(overwriteStr);
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("category").ascending().and(Sort.by("settingKey").ascending()));
            Page<SandboxSetting> settingsPage = configType != null
                    ? sandboxPropertyService.getSettings(search, category, configType, overwrite, pageable)
                    : sandboxPropertyService.getSettings(search, category, overwrite, pageable);

            model.addAttribute("settingsPage", settingsPage);
            model.addAttribute("settings", settingsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", settingsPage.getTotalPages());
            model.addAttribute("totalSettings", settingsPage.getTotalElements());
            model.addAttribute("search", search != null ? search : "");
            model.addAttribute("selectedCategory", category != null ? category : "");
            model.addAttribute("selectedConfigType", configTypeStr != null ? configTypeStr : "");
            model.addAttribute("selectedOverwrite", overwriteStr != null ? overwriteStr : "");

            // Stats
            model.addAttribute("totalCount", sandboxPropertyService.getTotalCount());
            model.addAttribute("modifiedCount", sandboxPropertyService.getModifiedCount());
            model.addAttribute("overwriteCount", sandboxPropertyService.getOverwriteCount());

            // Config types for filter dropdown
            model.addAttribute("configTypes", ConfigType.values());

            // Categories for filter dropdown
            model.addAttribute("categories", configType != null
                    ? sandboxPropertyService.getCategoriesByConfigType(configType)
                    : sandboxPropertyService.getAllCategories());

            return "sandbox-management";
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao carregar configurações: " + e.getMessage());
            return "sandbox-management";
        }
    }

    // ==================== REST API ====================

    /**
     * Update a setting's appliedValue and overwriteAtStartup flag.
     */
    @PostMapping("/api/settings/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSetting(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String newValue = (String) request.get("value");
            Boolean overwrite = request.containsKey("overwriteAtStartup")
                    ? Boolean.valueOf(request.get("overwriteAtStartup").toString())
                    : null;

            if (newValue == null || newValue.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Valor não pode ser vazio"
                ));
            }

            SandboxSetting updated = sandboxPropertyService.updateSetting(id, newValue, overwrite);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Configuração atualizada com sucesso",
                    "setting", settingToMap(updated)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Toggle the overwriteAtStartup flag for a setting.
     */
    @PostMapping("/api/settings/{id}/toggle-overwrite")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleOverwrite(@PathVariable Long id) {
        try {
            SandboxSetting updated = sandboxPropertyService.toggleOverwrite(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Overwrite " + (updated.getOverwriteAtStartup() ? "ativado" : "desativado"),
                    "overwriteAtStartup", updated.getOverwriteAtStartup()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Clear the appliedValue (revert to original ini value).
     */
    @PostMapping("/api/settings/{id}/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearAppliedValue(@PathVariable Long id) {
        try {
            SandboxSetting updated = sandboxPropertyService.clearAppliedValue(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Valor aplicado removido, usando valor original do ini"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // ==================== HELPERS ====================

    private Map<String, Object> settingToMap(SandboxSetting s) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", s.getId());
        map.put("settingKey", s.getSettingKey());
        map.put("currentValue", s.getCurrentValue());
        map.put("appliedValue", s.getAppliedValue());
        map.put("overwriteAtStartup", s.getOverwriteAtStartup());
        map.put("category", s.getCategory());
        map.put("description", s.getDescription());
        map.put("configType", s.getConfigType() != null ? s.getConfigType().name() : null);
        return map;
    }
}
