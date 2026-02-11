package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Region;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.RegionCustomProperty;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.RegionCustomPropertyRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.RegionRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class RegionService {

    private static final Logger logger = Logger.getLogger(RegionService.class.getName());

    private final RegionRepository regionRepository;
    private final RegionCustomPropertyRepository customPropertyRepository;

    @Value("${region.manager.file.path:C:/Users/ricar/Zomboid/Lua/RegionManager_Regions.json}")
    private String regionManagerFilePath;

    public RegionService(RegionRepository regionRepository,
                         RegionCustomPropertyRepository customPropertyRepository) {
        this.regionRepository = regionRepository;
        this.customPropertyRepository = customPropertyRepository;
    }

    @Transactional
    public Region createRegion(Region region, List<String> propNames, List<String> propValues) {
        logger.info("Creating new region: " + region.getName() + " [" + region.getCode() + "]");
        Region saved = regionRepository.save(region);
        saveCustomProperties(saved, propNames, propValues);
        return saved;
    }

    public List<Region> getAllRegions() {
        return regionRepository.findAllByOrderByNameAsc();
    }

    public List<Region> getEnabledRegions() {
        return regionRepository.findByEnabledTrue();
    }

    public Page<Region> getRegionsPaginated(String search, Boolean enabledOnly, String category, Pageable pageable) {
        logger.info("Getting paginated regions - search: " + search + ", enabledOnly: " + enabledOnly + ", category: " + category);
        if (enabledOnly == null) { enabledOnly = false; }
        return regionRepository.searchRegions(search, enabledOnly, category, pageable);
    }

    public Optional<Region> getRegionById(Long id) {
        return regionRepository.findById(id);
    }

    public Optional<Region> getRegionByCode(String code) {
        return regionRepository.findByCode(code);
    }

    @Transactional
    public Region updateRegion(Long id, Region updatedRegion, List<String> propNames, List<String> propValues) {
        logger.info("Updating region with id: " + id);
        Region region = regionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found with id: " + id));

        region.setCode(updatedRegion.getCode());
        region.setName(updatedRegion.getName());
        region.setCategories(updatedRegion.getCategories());
        region.setX1(updatedRegion.getX1());
        region.setX2(updatedRegion.getX2());
        region.setY1(updatedRegion.getY1());
        region.setY2(updatedRegion.getY2());
        region.setZ(updatedRegion.getZ());
        region.setEnabled(updatedRegion.getEnabled());
        region.setPermanent(updatedRegion.getPermanent());

        // Replace custom properties
        region.clearCustomProperties();
        regionRepository.save(region); // flush removal
        saveCustomProperties(region, propNames, propValues);

        return region;
    }

    @Transactional
    public void deleteRegion(Long id) {
        logger.info("Deleting region with id: " + id);
        Region region = regionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found with id: " + id));
        if (region.getPermanent() != null && region.getPermanent()) {
            throw new IllegalStateException("Cannot delete a permanent region");
        }
        regionRepository.deleteById(id);
    }

    @Transactional
    public Region toggleEnabled(Long id) {
        logger.info("Toggling enabled for region with id: " + id);
        Region region = regionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Region not found with id: " + id));
        region.setEnabled(!region.getEnabled());
        return regionRepository.save(region);
    }

    public long getTotalRegionCount() {
        return regionRepository.count();
    }

    public long getEnabledRegionCount() {
        return regionRepository.findByEnabledTrue().size();
    }

    /**
     * Automatically writes regions JSON file on application startup.
     * This ensures the mod always has the latest regions configuration.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void writeRegionsOnStartup() {
        try {
            writeRegionsJsonToFile();
            logger.info("Regions JSON file automatically written on startup");
        } catch (IOException e) {
            logger.severe("Failed to write regions JSON file on startup: " + e.getMessage());
        }
    }

    /**
     * Generates the JSON content for the RegionManager_Regions.json file
     * using only enabled regions that are either permanent or not expired.
     */
    public String generateRegionsJson() {
        List<Region> enabledRegions = regionRepository.findByEnabledTrue();
        LocalDate today = LocalDate.now();
        
        // Filter: include only permanent regions OR regions that haven't expired yet
        List<Region> validRegions = enabledRegions.stream()
            .filter(region -> {
                if (Boolean.TRUE.equals(region.getPermanent())) {
                    return true; // Permanent regions always included
                }
                // Non-permanent regions must not be expired
                return region.getExpirationDate() == null || region.getExpirationDate().isAfter(today);
            })
            .toList();
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject root = new JsonObject();
        root.addProperty("lastUpdated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        JsonArray regionsArray = new JsonArray();
        for (Region region : validRegions) {
            JsonObject regionObj = new JsonObject();
            regionObj.addProperty("id", region.getCode());
            regionObj.addProperty("name", region.getName());

            // Categories array
            JsonArray categoriesArray = new JsonArray();
            for (String cat : region.getCategoryArray()) {
                String trimmed = cat.trim();
                if (!trimmed.isEmpty()) {
                    categoriesArray.add(trimmed);
                }
            }
            regionObj.add("categories", categoriesArray);

            // Custom properties as object
            JsonObject customPropsObj = new JsonObject();
            for (RegionCustomProperty prop : region.getCustomProperties()) {
                // Try to parse as number or boolean, otherwise keep as string
                String val = prop.getValue();
                if ("true".equalsIgnoreCase(val) || "false".equalsIgnoreCase(val)) {
                    customPropsObj.addProperty(prop.getName(), Boolean.parseBoolean(val));
                } else {
                    try {
                        int intVal = Integer.parseInt(val);
                        customPropsObj.addProperty(prop.getName(), intVal);
                    } catch (NumberFormatException e) {
                        try {
                            double doubleVal = Double.parseDouble(val);
                            customPropsObj.addProperty(prop.getName(), doubleVal);
                        } catch (NumberFormatException e2) {
                            customPropsObj.addProperty(prop.getName(), val);
                        }
                    }
                }
            }
            regionObj.add("customProperties", customPropsObj);

            regionObj.addProperty("enabled", region.getEnabled());
            regionObj.addProperty("x1", region.getX1());
            regionObj.addProperty("x2", region.getX2());
            regionObj.addProperty("y1", region.getY1());
            regionObj.addProperty("y2", region.getY2());
            regionObj.addProperty("z", region.getZ());

            regionsArray.add(regionObj);
        }

        root.add("regions", regionsArray);
        root.addProperty("version", "1.0");

        return gson.toJson(root);
    }

    /**
     * Writes the regions JSON directly to the configured file path where the mod expects it.
     * Creates parent directories if they don't exist.
     * 
     * @throws IOException if there's an error writing the file
     */
    public void writeRegionsJsonToFile() throws IOException {
        String json = generateRegionsJson();
        Path filePath = Paths.get(regionManagerFilePath);
        
        // Create parent directories if they don't exist
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
            logger.info("Created directory: " + parentDir);
        }
        
        // Write the JSON to the file
        Files.writeString(filePath, json);
        logger.info("Successfully wrote regions JSON to: " + regionManagerFilePath);
    }

    private void saveCustomProperties(Region region, List<String> propNames, List<String> propValues) {
        if (propNames != null && propValues != null) {
            int size = Math.min(propNames.size(), propValues.size());
            for (int i = 0; i < size; i++) {
                String name = propNames.get(i) != null ? propNames.get(i).trim() : "";
                String value = propValues.get(i) != null ? propValues.get(i).trim() : "";
                if (!name.isEmpty()) {
                    RegionCustomProperty prop = new RegionCustomProperty(name, value);
                    region.addCustomProperty(prop);
                    customPropertyRepository.save(prop);
                }
            }
        }
    }
}
