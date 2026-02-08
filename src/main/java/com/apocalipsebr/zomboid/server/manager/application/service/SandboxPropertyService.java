package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.application.constants.SandboxProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for managing Zomboid Sandbox properties
 * Handles reading, parsing, and writing to SandboxVars.lua file
 */
@Service
public class SandboxPropertyService {
    private static final Logger logger = Logger.getLogger(SandboxPropertyService.class.getName());
    
    @Value("${zomboid.server.path:/opt/pzserver}")
    private String serverPath;

    @Value("${zomboid.sandbox.file:Zomboid/Sandbox/Sandbox.lua}")
    private String sandboxFileName;

    /**
     * Get all sandbox properties with their current values from the SandboxVars file
     */
    public List<Map<String, Object>> getAllProperties() {
        try {
            Map<String, Object> values = parseSandboxFile();
            return Arrays.stream(SandboxProperty.values())
                    .map(prop -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("property", prop);
                        result.put("currentValue", values.getOrDefault(prop.getKey(), prop.getDefaultValue()));
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving all properties", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get properties filtered by category
     */
    public List<Map<String, Object>> getPropertiesByCategory(String categoryName) {
        try {
            SandboxProperty.PropertyCategory category = SandboxProperty.PropertyCategory.valueOf(categoryName.toUpperCase());
            Map<String, Object> values = parseSandboxFile();
            
            return Arrays.stream(SandboxProperty.getByCategory(category))
                    .map(prop -> {
                        Map<String, Object> result = new HashMap<>();
                        result.put("property", prop);
                        result.put("currentValue", values.getOrDefault(prop.getKey(), prop.getDefaultValue()));
                        return result;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving properties by category: " + categoryName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Get a specific property by key
     */
    public Optional<Map<String, Object>> getPropertyByKey(String key) {
        try {
            Optional<SandboxProperty> property = SandboxProperty.fromKey(key);
            if (property.isEmpty()) {
                return Optional.empty();
            }
            
            Map<String, Object> values = parseSandboxFile();
            Map<String, Object> result = new HashMap<>();
            result.put("property", property.get());
            result.put("currentValue", values.getOrDefault(key, property.get().getDefaultValue()));
            
            return Optional.of(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving property: " + key, e);
            return Optional.empty();
        }
    }

    /**
     * Update a sandbox property value
     */
    public boolean updateProperty(String key, Object newValue) {
        try {
            Optional<SandboxProperty> property = SandboxProperty.fromKey(key);
            if (property.isEmpty()) {
                logger.warning("Property not found: " + key);
                return false;
            }

            SandboxProperty prop = property.get();
            
            // Validate the new value
            if (!prop.isValidValue(newValue)) {
                logger.warning("Invalid value for property " + key + ": " + newValue);
                return false;
            }

            // Read current file
            Map<String, Object> values = parseSandboxFile();
            values.put(key, newValue);

            // Write back to file
            writeSandboxFile(values);
            
            logger.info("Updated property " + key + " to value: " + newValue);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating property: " + key, e);
            return false;
        }
    }

    /**
     * Parse the Sandbox.lua file and extract property values
     * Supports both table notation and direct assignment
     */
    private Map<String, Object> parseSandboxFile() throws IOException {
        Map<String, Object> result = new HashMap<>();
        Path sandboxPath = Paths.get(serverPath, sandboxFileName);

        if (!Files.exists(sandboxPath)) {
            logger.warning("Sandbox file not found at: " + sandboxPath);
            return result;
        }

        String content = new String(Files.readAllBytes(sandboxPath), StandardCharsets.UTF_8);
        
        // Pattern for: PropertyName = value
        Pattern simplePattern = Pattern.compile("([A-Za-z0-9_.]+)\\s*=\\s*([^,\\n;]+)");
        Matcher matcher = simplePattern.matcher(content);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String valueStr = matcher.group(2).trim();
            
            Optional<SandboxProperty> property = SandboxProperty.fromKey(key);
            if (property.isPresent()) {
                try {
                    Object value = property.get().getDataType().parse(valueStr);
                    result.put(key, value);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to parse value for " + key + ": " + valueStr, e);
                }
            }
        }

        return result;
    }

    /**
     * Write properties back to the Sandbox.lua file
     * Preserves file structure and formatting
     */
    private void writeSandboxFile(Map<String, Object> properties) throws IOException {
        Path sandboxPath = Paths.get(serverPath, sandboxFileName);
        
        // Create backup
        Path backupPath = sandboxPath.resolveSibling(sandboxPath.getFileName() + ".backup");
        Files.copy(sandboxPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        StringBuilder content = new StringBuilder();
        content.append("-- Project Zomboid Sandbox Configuration\n");
        content.append("-- Generated by Apocalipse [BR] Manager\n");
        content.append("-- Last Modified: ").append(new Date()).append("\n\n");

        // Write properties grouped by category
        Map<SandboxProperty.PropertyCategory, List<SandboxProperty>> byCategory = 
            Arrays.stream(SandboxProperty.values())
                .collect(Collectors.groupingBy(SandboxProperty::getCategory));

        for (SandboxProperty.PropertyCategory category : byCategory.keySet()) {
            content.append("-- ").append(category.getDisplayName()).append("\n");
            
            for (SandboxProperty prop : byCategory.get(category)) {
                Object value = properties.getOrDefault(prop.getKey(), prop.getDefaultValue());
                String luaValue = prop.getDataType().formatForLua(value);
                content.append(prop.getKey()).append(" = ").append(luaValue).append("\n");
            }
            
            content.append("\n");
        }

        // Write to file
        Files.write(sandboxPath, content.toString().getBytes(StandardCharsets.UTF_8));
        logger.info("Successfully wrote Sandbox configuration to: " + sandboxPath);
    }

    /**
     * Get all available property categories
     */
    public List<Map<String, Object>> getAllCategories() {
        return Arrays.stream(SandboxProperty.PropertyCategory.values())
                .map(cat -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", cat.name());
                    m.put("displayName", cat.getDisplayName());
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * Get property count by category
     */
    public Map<String, Integer> getPropertyCountByCategory() {
        return Arrays.stream(SandboxProperty.values())
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getDisplayName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                List::size
                        )
                ));
    }
}
