package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.*;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterMigrationRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class MigrationService {

    private static final Logger logger = Logger.getLogger(MigrationService.class.getName());

    private final CharacterMigrationRepository migrationRepository;
    private final ClaimedCarRepository claimedCarRepository;
    private final CharacterService characterService;
    private final ServerCommandService serverCommandService;
    private final SeasonService seasonService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${vehicle.migration.file.path:}")
    private String vehicleMigrationFilePath;

    /**
     * Ordered list of skill keys matching the game's `addxp` parameter names.
     * Each entry maps a game skill name to the corresponding getter on the Character entity.
     */
    private static final List<SkillMapping> SKILL_MAPPINGS = List.of(
            new SkillMapping("Cooking", "skillCooking"),
            new SkillMapping("Fitness", "skillFitness"),
            new SkillMapping("Strength", "skillStrength"),
            new SkillMapping("Blunt", "skillBlunt"),
            new SkillMapping("Axe", "skillAxe"),
            new SkillMapping("Lightfoot", "skillLightfoot"),
            new SkillMapping("Nimble", "skillNimble"),
            new SkillMapping("Sprinting", "skillSprinting"),
            new SkillMapping("Sneak", "skillSneak"),
            new SkillMapping("Woodwork", "skillWoodwork"),
            new SkillMapping("Aiming", "skillAiming"),
            new SkillMapping("Reloading", "skillReloading"),
            new SkillMapping("Farming", "skillFarming"),
            new SkillMapping("Fishing", "skillFishing"),
            new SkillMapping("Trapping", "skillTrapping"),
            new SkillMapping("PlantScavenging", "skillPlantScavenging"),
            new SkillMapping("Doctor", "skillDoctor"),
            new SkillMapping("Electricity", "skillElectricity"),
            new SkillMapping("Blacksmith", "skillBlacksmith"),
            new SkillMapping("MetalWelding", "skillMetalWelding"),
            new SkillMapping("Mechanics", "skillMechanics"),
            new SkillMapping("Spear", "skillSpear"),
            new SkillMapping("Maintenance", "skillMaintenance"),
            new SkillMapping("SmallBlade", "skillSmallBlade"),
            new SkillMapping("LongBlade", "skillLongBlade"),
            new SkillMapping("SmallBlunt", "skillSmallBlunt"),
            new SkillMapping("Tailoring", "skillTailoring"),
            new SkillMapping("Tracking", "skillTracking"),
            new SkillMapping("Husbandry", "skillHusbandry"),
            new SkillMapping("FlintKnapping", "skillFlintKnapping"),
            new SkillMapping("Masonry", "skillMasonry"),
            new SkillMapping("Pottery", "skillPottery"),
            new SkillMapping("Carving", "skillCarving"),
            new SkillMapping("Butchering", "skillButchering"),
            new SkillMapping("Glassmaking", "skillGlassmaking")
    );

    public MigrationService(CharacterMigrationRepository migrationRepository,
                            ClaimedCarRepository claimedCarRepository,
                            CharacterService characterService,
                            ServerCommandService serverCommandService,
                            SeasonService seasonService) {
        this.migrationRepository = migrationRepository;
        this.claimedCarRepository = claimedCarRepository;
        this.characterService = characterService;
        this.serverCommandService = serverCommandService;
        this.seasonService = seasonService;
    }

    // ─────────────────────────────────────────────────────────────
    //  CHARACTER XP MIGRATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Queue a character's XP for migration. Takes a snapshot of the stored skill
     * XP values and creates a PENDING migration record. The actual XP grant happens
     * on the next heartbeat via {@link #processPendingMigration}.
     */
    @Transactional
    public CharacterMigration requestCharacterMigration(User user, Long characterId) {
        Character character = characterService.getCharacterById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Personagem não encontrado"));

        if (!character.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Personagem não pertence ao usuário");
        }

        Season currentSeason = seasonService.getCurrentSeason();
        if (character.getSeason() == null || !character.getSeason().getId().equals(currentSeason.getId())) {
            throw new IllegalArgumentException("Migração disponível apenas para personagens da temporada atual");
        }

        if (migrationRepository.existsByCharacterId(characterId)) {
            throw new IllegalArgumentException("Este personagem já possui uma migração registrada");
        }

        // Snapshot current skill XP values
        Map<String, Double> snapshot = getSkillSnapshot(character);
        String snapshotJson;
        try {
            snapshotJson = objectMapper.writeValueAsString(snapshot);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao serializar snapshot de skills", e);
        }

        CharacterMigration migration = new CharacterMigration(character, user, snapshotJson);
        return migrationRepository.save(migration);
    }

    /**
     * Called from the heartbeat flow ({@code CharacterService.updateCharacterStats}).
     * Checks if the character has a PENDING migration and, if so, computes the XP
     * difference and issues {@code addxp} commands via RCON.
     *
     * @param character     the persisted character (already updated with current heartbeat data)
     * @param currentSkills the skill map from the current heartbeat DTO (live in-game values)
     */
    @Transactional
    public void processPendingMigration(Character character, Map<String, Double> currentSkills) {
        Optional<CharacterMigration> opt = migrationRepository.findByCharacterIdAndStatus(
                character.getId(), MigrationStatus.PENDING.name());

        if (opt.isEmpty()) {
            return; // no pending migration for this character
        }

        CharacterMigration migration = opt.get();
        migration.setMigrationStatus(MigrationStatus.PROCESSING);
        migrationRepository.save(migration);

        try {
            Map<String, Double> snapshot = objectMapper.readValue(
                    migration.getSnapshotSkills(), new TypeReference<Map<String, Double>>() {});

            Map<String, Double> appliedDeltas = new LinkedHashMap<>();
            String playerName = character.getPlayerName();

            for (SkillMapping mapping : SKILL_MAPPINGS) {
                double stored = snapshot.getOrDefault(mapping.gameKey, 0.0);
                double current = (currentSkills != null)
                        ? currentSkills.getOrDefault(mapping.gameKey, 0.0) : 0.0;
                double delta = stored - current;

                if (delta > 0) {
                    int xpToGrant = (int) Math.round(delta);
                    String command = "addxp \"" + playerName + "\" " + mapping.gameKey + "=" + xpToGrant + " -false";
                    logger.info("Migration addxp: " + command);
                    serverCommandService.sendCommand(command);
                    appliedDeltas.put(mapping.gameKey, (double) xpToGrant);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Migration interrupted", e);
                    }
                }
            }

            migration.setAppliedSkills(objectMapper.writeValueAsString(appliedDeltas));
            migration.setMigrationStatus(MigrationStatus.COMPLETED);
            migration.setProcessedAt(LocalDateTime.now());
            migrationRepository.save(migration);

            logger.info("Character migration completed for '" + playerName + "' (id=" + character.getId() + ")");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Character migration failed for character id=" + character.getId(), e);
            migration.setMigrationStatus(MigrationStatus.FAILED);
            migration.setErrorMessage(e.getMessage());
            migration.setProcessedAt(LocalDateTime.now());
            migrationRepository.save(migration);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CAR MIGRATION
    // ─────────────────────────────────────────────────────────────

    /**
     * Migrate a single claimed car: write its data to the VehicleMigration.jsonl file
     * at the user-selected map coordinates, then delete the car from the database.
     *
     * @param user  the authenticated user
     * @param carId the claimed car ID
     * @param x     world X coordinate chosen on the map
     * @param y     world Y coordinate chosen on the map
     */
    @Transactional
    public void migrateClaimedCar(User user, Long carId, double x, double y) {
        ClaimedCar car = claimedCarRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Veículo não encontrado"));

        if (car.getUser() == null || !car.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Veículo não pertence ao usuário");
        }

        if (!Boolean.TRUE.equals(car.getPreservedForMigration())) {
            throw new IllegalArgumentException("Veículo não está marcado para migração");
        }

        // Build the JSON entry using user-selected coordinates
        Map<String, Object> vehicleEntry = new LinkedHashMap<>();
        vehicleEntry.put("scriptName", car.getScriptName());
        vehicleEntry.put("x", x + 0.5); // center of tile
        vehicleEntry.put("y", y + 0.37);

        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (car.getItems() != null) {
            for (ClaimedCarItem item : car.getItems()) {
                Map<String, Object> itemEntry = new LinkedHashMap<>();
                itemEntry.put("fullType", item.getFullType());
                itemEntry.put("count", item.getCount());
                itemEntry.put("container", item.getContainer() != null ? item.getContainer() : "TruckBed");
                itemsList.add(itemEntry);
            }
        }
        vehicleEntry.put("items", itemsList);

        // Write to the JSONL file (append mode, synchronized)
        writeVehicleJsonl(vehicleEntry);

        // Delete the car from the database (cascade removes items)
        claimedCarRepository.delete(car);

        logger.info("Car migration completed: car id=" + carId + " scriptName=" + car.getScriptName()
                + " spawned at (" + x + ", " + y + ")");
    }

    private synchronized void writeVehicleJsonl(Map<String, Object> vehicleEntry) {
        if (vehicleMigrationFilePath == null || vehicleMigrationFilePath.isBlank()) {
            throw new IllegalStateException("vehicle.migration.file.path não configurado");
        }

        try {
            Path filePath = Paths.get(vehicleMigrationFilePath);

            // Create parent directories if they don't exist
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            String jsonLine = objectMapper.writeValueAsString(vehicleEntry) + System.lineSeparator();
            Files.write(filePath, jsonLine.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            throw new RuntimeException("Falha ao escrever no arquivo de migração de veículos: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  QUERY HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns all preserved cars belonging to the given user.
     */
    public List<ClaimedCar> getPreservedCarsForUser(User user) {
        return claimedCarRepository.findByUser(user).stream()
                .filter(c -> Boolean.TRUE.equals(c.getPreservedForMigration()))
                .toList();
    }

    /**
     * Returns all migration records for a user, mapped by character ID for easy template lookup.
     */
    public Map<Long, CharacterMigration> getUserMigrationsMap(User user) {
        List<CharacterMigration> migrations = migrationRepository.findByUserId(user.getId());
        Map<Long, CharacterMigration> map = new HashMap<>();
        for (CharacterMigration m : migrations) {
            map.put(m.getCharacter().getId(), m);
        }
        return map;
    }

    /**
     * Returns all migration records for a user.
     */
    public List<CharacterMigration> getUserMigrations(User user) {
        return migrationRepository.findByUserId(user.getId());
    }

    /**
     * Check if a character already has any migration record (regardless of status).
     */
    public boolean hasMigration(Long characterId) {
        return migrationRepository.existsByCharacterId(characterId);
    }

    // ─────────────────────────────────────────────────────────────
    //  SKILL SNAPSHOT
    // ─────────────────────────────────────────────────────────────

    /**
     * Reads all 35 skill values from a character entity into a map using the game's
     * canonical skill key names (e.g. "Woodwork", "Cooking", "Fitness").
     */
    public static Map<String, Double> getSkillSnapshot(Character character) {
        Map<String, Double> snapshot = new LinkedHashMap<>();
        snapshot.put("Cooking", safe(character.getSkillCooking()));
        snapshot.put("Fitness", safe(character.getSkillFitness()));
        snapshot.put("Strength", safe(character.getSkillStrength()));
        snapshot.put("Blunt", safe(character.getSkillBlunt()));
        snapshot.put("Axe", safe(character.getSkillAxe()));
        snapshot.put("Lightfoot", safe(character.getSkillLightfoot()));
        snapshot.put("Nimble", safe(character.getSkillNimble()));
        snapshot.put("Sprinting", safe(character.getSkillSprinting()));
        snapshot.put("Sneak", safe(character.getSkillSneak()));
        snapshot.put("Woodwork", safe(character.getSkillWoodwork()));
        snapshot.put("Aiming", safe(character.getSkillAiming()));
        snapshot.put("Reloading", safe(character.getSkillReloading()));
        snapshot.put("Farming", safe(character.getSkillFarming()));
        snapshot.put("Fishing", safe(character.getSkillFishing()));
        snapshot.put("Trapping", safe(character.getSkillTrapping()));
        snapshot.put("PlantScavenging", safe(character.getSkillPlantScavenging()));
        snapshot.put("Doctor", safe(character.getSkillDoctor()));
        snapshot.put("Electricity", safe(character.getSkillElectricity()));
        snapshot.put("Blacksmith", safe(character.getSkillBlacksmith()));
        snapshot.put("MetalWelding", safe(character.getSkillMetalWelding()));
        snapshot.put("Mechanics", safe(character.getSkillMechanics()));
        snapshot.put("Spear", safe(character.getSkillSpear()));
        snapshot.put("Maintenance", safe(character.getSkillMaintenance()));
        snapshot.put("SmallBlade", safe(character.getSkillSmallBlade()));
        snapshot.put("LongBlade", safe(character.getSkillLongBlade()));
        snapshot.put("SmallBlunt", safe(character.getSkillSmallBlunt()));
        snapshot.put("Tailoring", safe(character.getSkillTailoring()));
        snapshot.put("Tracking", safe(character.getSkillTracking()));
        snapshot.put("Husbandry", safe(character.getSkillHusbandry()));
        snapshot.put("FlintKnapping", safe(character.getSkillFlintKnapping()));
        snapshot.put("Masonry", safe(character.getSkillMasonry()));
        snapshot.put("Pottery", safe(character.getSkillPottery()));
        snapshot.put("Carving", safe(character.getSkillCarving()));
        snapshot.put("Butchering", safe(character.getSkillButchering()));
        snapshot.put("Glassmaking", safe(character.getSkillGlassmaking()));
        return snapshot;
    }

    private static double safe(Double value) {
        return value != null ? value : 0.0;
    }

    /**
     * Internal record to map game skill names to entity field names.
     */
    private record SkillMapping(String gameKey, String fieldName) {}
}
