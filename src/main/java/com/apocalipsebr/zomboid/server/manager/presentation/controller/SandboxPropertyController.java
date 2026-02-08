package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.constants.SandboxProperty;
import com.apocalipsebr.zomboid.server.manager.application.service.SandboxPropertyService;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.SandboxPropertyDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for managing Zomboid Sandbox Properties
 */
@Controller
@RequestMapping("/admin/sandbox")
@PreAuthorize("hasRole('ADMIN')")
public class SandboxPropertyController {
    
    private final SandboxPropertyService sandboxPropertyService;

    public SandboxPropertyController(SandboxPropertyService sandboxPropertyService) {
        this.sandboxPropertyService = sandboxPropertyService;
    }

    /**
     * Display the sandbox property management view
     */
    @GetMapping
    public String showSandboxManagement(
            @RequestParam(value = "category", required = false) String category,
            Model model) {
        try {
            // Get all categories
            List<Map<String, Object>> categories = sandboxPropertyService.getAllCategories();
            model.addAttribute("categories", categories);

            // Get property count by category
            Map<String, Integer> countByCategory = sandboxPropertyService.getPropertyCountByCategory();
            model.addAttribute("countByCategory", countByCategory);

            // Get properties (filtered by category if provided)
            List<Map<String, Object>> properties;
            if (category != null && !category.isEmpty()) {
                properties = sandboxPropertyService.getPropertiesByCategory(category);
                model.addAttribute("selectedCategory", category);
            } else {
                properties = sandboxPropertyService.getAllProperties();
            }

            // Convert to DTOs
            List<SandboxPropertyDTO> propertyDTOs = properties.stream()
                    .map(m -> {
                        SandboxProperty prop = (SandboxProperty) m.get("property");
                        Object currentValue = m.get("currentValue");
                        return new SandboxPropertyDTO(prop, currentValue);
                    })
                    .collect(Collectors.toList());

            model.addAttribute("properties", propertyDTOs);
            model.addAttribute("totalProperties", propertyDTOs.size());

            return "sandbox-management";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading sandbox properties: " + e.getMessage());
            return "sandbox-management";
        }
    }

    /**
     * REST API: Get all properties as JSON
     */
    @GetMapping("/api/properties")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllProperties(
            @RequestParam(value = "category", required = false) String category) {
        try {
            List<Map<String, Object>> properties;
            
            if (category != null && !category.isEmpty()) {
                properties = sandboxPropertyService.getPropertiesByCategory(category);
            } else {
                properties = sandboxPropertyService.getAllProperties();
            }

            List<SandboxPropertyDTO> dtos = properties.stream()
                    .map(m -> {
                        SandboxProperty prop = (SandboxProperty) m.get("property");
                        Object currentValue = m.get("currentValue");
                        return new SandboxPropertyDTO(prop, currentValue);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "count", dtos.size(),
                    "properties", dtos
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * REST API: Get specific property by key
     */
    @GetMapping("/api/properties/{key}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPropertyByKey(@PathVariable String key) {
        try {
            Optional<Map<String, Object>> property = sandboxPropertyService.getPropertyByKey(key);
            
            if (property.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "error", "Property not found: " + key
                        ));
            }

            Map<String, Object> prop = property.get();
            SandboxProperty sandboxProp = (SandboxProperty) prop.get("property");
            Object currentValue = prop.get("currentValue");
            SandboxPropertyDTO dto = new SandboxPropertyDTO(sandboxProp, currentValue);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "property", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * REST API: Update a property value
     */
    @PostMapping("/api/properties/{key}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateProperty(
            @PathVariable String key,
            @RequestBody Map<String, Object> request) {
        try {
            Object newValue = request.get("value");

            if (newValue == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Value cannot be null"
                        ));
            }

            // Get property to validate
            Optional<SandboxProperty> property = SandboxProperty.fromKey(key);
            if (property.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "success", false,
                                "error", "Property not found: " + key
                        ));
            }

            // Convert value to correct type
            Object convertedValue = property.get().getDataType().parse(newValue.toString());

            // Update property
            boolean success = sandboxPropertyService.updateProperty(key, convertedValue);

            if (!success) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "error", "Failed to update property. Invalid value or constraint violation."
                        ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Property updated successfully",
                    "key", key,
                    "newValue", convertedValue
            ));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Invalid value format: " + e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }

    /**
     * REST API: Get all available categories
     */
    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            List<Map<String, Object>> categories = sandboxPropertyService.getAllCategories();
            Map<String, Integer> countByCategory = sandboxPropertyService.getPropertyCountByCategory();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "categories", categories,
                    "countByCategory", countByCategory
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", e.getMessage()
                    ));
        }
    }
}
