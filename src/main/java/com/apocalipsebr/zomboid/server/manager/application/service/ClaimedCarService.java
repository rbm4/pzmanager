package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCarItem;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarRepository;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ClaimedCarService {

    private static final Logger logger = Logger.getLogger(ClaimedCarService.class.getName());

    private final ClaimedCarRepository claimedCarRepository;
    private final UserService userService;
    private final Gson gson = new Gson();

    @Value("${player.claimed.car.file.path:}")
    private String claimedCarFilePath;

    public ClaimedCarService(ClaimedCarRepository claimedCarRepository, UserService userService) {
        this.claimedCarRepository = claimedCarRepository;
        this.userService = userService;
    }

    /**
     * Scheduled task that reads the VehicleClaimSystemDatabase.json file every minute
     * and syncs the claimed cars data into the database.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncClaimedCarsFromFile() {
        if (claimedCarFilePath == null || claimedCarFilePath.isBlank()) {
            logger.warning("Claimed car file path is not configured. Skipping sync.");
            return;
        }

        Path filePath = Paths.get(claimedCarFilePath);
        if (!Files.exists(filePath)) {
            logger.warning("Claimed car file not found at: " + claimedCarFilePath + ". Skipping sync.");
            return;
        }

        try {
            String jsonContent = Files.readString(filePath);
            if (jsonContent == null || jsonContent.isBlank()) {
                logger.info("Claimed car file is empty. Skipping sync.");
                return;
            }

            JsonObject root = gson.fromJson(jsonContent, JsonObject.class);
            if (root == null || root.isEmpty()) {
                logger.info("Claimed car JSON has no entries. Skipping sync.");
                return;
            }

            int syncedCount = 0;
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                try {
                    JsonObject carJson = entry.getValue().getAsJsonObject();
                    syncSingleCar(carJson);
                    syncedCount++;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to sync claimed car with key: " + entry.getKey(), e);
                }
            }

            logger.info("Claimed cars sync completed. Synced " + syncedCount + " vehicles.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error reading claimed car file: " + claimedCarFilePath, e);
        }
    }

    /**
     * Syncs a single car entry from the JSON into the database.
     * Creates or updates the ClaimedCar and fully replaces its items.
     */
    private void syncSingleCar(JsonObject carJson) {
        String vehicleHash = getStringOrNull(carJson, "vehicleHash");
        if (vehicleHash == null || vehicleHash.isBlank()) {
            logger.warning("Skipping car entry with missing vehicleHash.");
            return;
        }

        // Find existing or create new
        Optional<ClaimedCar> existingOpt = claimedCarRepository.findByVehicleHash(vehicleHash);
        ClaimedCar car = existingOpt.orElseGet(() -> {
            ClaimedCar newCar = new ClaimedCar();
            newCar.setVehicleHash(vehicleHash);
            return newCar;
        });

        // Update fields from JSON
        String ownerSteamId = getStringOrNull(carJson, "ownerSteamID");
        car.setOwnerSteamId(ownerSteamId != null ? ownerSteamId : "");
        car.setOwnerName(getStringOrNull(carJson, "ownerName"));
        car.setVehicleName(getStringOrNull(carJson, "vehicleName"));
        car.setScriptName(getStringOrNull(carJson, "scriptName"));
        car.setX(getDoubleOrNull(carJson, "x"));
        car.setY(getDoubleOrNull(carJson, "y"));
        car.setLastUpdated(getLongOrNull(carJson, "lastUpdated"));
        car.setUpdatedAt(LocalDateTime.now());

        // Resolve User from the ownerSteamID using approximate match (±10 tolerance)
        if (ownerSteamId != null && !ownerSteamId.isBlank()) {
            try {
                BigDecimal steamIdDecimal = new BigDecimal(ownerSteamId);
                User user = userService.createOrGetUserBySteamId(steamIdDecimal);
                car.setUser(user);
            } catch (NumberFormatException e) {
                logger.warning("Invalid ownerSteamID format for vehicle " + vehicleHash + ": " + ownerSteamId);
            }
        }

        // Full replace items: clear existing and re-add from JSON
        car.clearItems();

        if (carJson.has("items") && carJson.get("items").isJsonArray()) {
            JsonArray itemsArray = carJson.getAsJsonArray("items");
            for (JsonElement itemElement : itemsArray) {
                JsonObject itemJson = itemElement.getAsJsonObject();
                ClaimedCarItem item = new ClaimedCarItem(
                        getStringOrNull(itemJson, "fullType"),
                        getIntOrDefault(itemJson, "count", 1),
                        getStringOrNull(itemJson, "container")
                );
                car.addItem(item);
            }
        }

        claimedCarRepository.save(car);
    }

    // --- Query methods ---

    public List<ClaimedCar> getClaimedCarsByUser(User user) {
        return claimedCarRepository.findByUser(user);
    }

    public List<ClaimedCar> getClaimedCarsByUserId(Long userId) {
        return claimedCarRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    public List<ClaimedCar> getAllClaimedCars() {
        return claimedCarRepository.findAll();
    }

    public Optional<ClaimedCar> getClaimedCarById(Long id) {
        return claimedCarRepository.findById(id);
    }

    public List<ClaimedCar> getUnmigratedCars() {
        return claimedCarRepository.findByMigratedFalse();
    }

    // --- JSON helper methods ---

    private String getStringOrNull(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsString();
        }
        return null;
    }

    private Double getDoubleOrNull(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsDouble();
        }
        return null;
    }

    private Long getLongOrNull(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsLong();
        }
        return null;
    }

    private int getIntOrDefault(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsInt();
        }
        return defaultValue;
    }
}
