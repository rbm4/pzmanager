package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.UserService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.ZombieKillsUpdateDTO;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/zombie-kills")
public class ZombieKillsController {

    private static final Logger logger = Logger.getLogger(ZombieKillsController.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final UserService userService;
    private final CharacterService characterService;
    private static final Gson objectMapper = new Gson();

    @Value("${zombie.kills.file.path:C:\\Users\\ricar\\Zomboid\\Lua\\ZKC_PlayerData.jsonl}")
    private String zombieKillsFilePath;

    public ZombieKillsController(UserService userService, CharacterService characterService) {
        this.userService = userService;
        this.characterService = characterService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleConsumeKills() {

        scheduler.schedule(() -> {
            updateZombieKills();
        }, 1, TimeUnit.MINUTES);
    }

    @PostMapping("/consume")
    public ResponseEntity<Map<String, Object>> updateZombieKills() {
        Map<String, Object> response = new HashMap<>();

        try {
            File file = new File(zombieKillsFilePath);

            // Check if file exists
            if (!file.exists()) {
                response.put("success", false);
                response.put("message", "No zombie kills file found");
                response.put("processed", 0);
                return ResponseEntity.ok(response);
            }

            logger.info("Reading zombie kills data from file: " + zombieKillsFilePath);

            // Read file content
            String fileContent = new String(Files.readAllBytes(Paths.get(zombieKillsFilePath)));

            if (fileContent.trim().isEmpty()) {
                response.put("success", true);
                response.put("message", "File is empty, nothing to process");
                response.put("processed", 0);
                return ResponseEntity.ok(response);
            }

            // Parse JSONL format - each line is a separate JSON object
            ZombieKillsUpdateDTO[] updateDTOs;
            String[] lines = fileContent.split("\n");
            java.util.List<ZombieKillsUpdateDTO> dtoList = new java.util.ArrayList<>();

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    ZombieKillsUpdateDTO dto = objectMapper.fromJson(trimmedLine, ZombieKillsUpdateDTO.class);
                    long high = Long.parseLong(dto.playerIdHigh());
                    long low = Long.parseLong(dto.playerIdLow());
                    String fullSteamId = String.valueOf((high * 4294967296L) + low);
                    dto = dto.withPlayerIdNumeric(new BigDecimal(fullSteamId))
                            .withTimestampNumeric(new BigDecimal(dto.timestamp()));
                    dtoList.add(dto);
                }
            }

            updateDTOs = dtoList.toArray(new ZombieKillsUpdateDTO[0]);

            int processedCount = 0;
            List<Map<String, Object>> processedEntries = new java.util.ArrayList<>();

            // Process each zombie kills update
            for (ZombieKillsUpdateDTO updateDTO : updateDTOs) {
                Map<String, Object> entryResult = processZombieKillsEntry(updateDTO);
                processedEntries.add(entryResult);
                if ((boolean) entryResult.get("success")) {
                    processedCount++;
                }
            }

            // Clear file after successful processing
            Files.write(Paths.get(zombieKillsFilePath), "".getBytes());
            logger.info("Cleared zombie kills file after processing");

            response.put("success", true);
            response.put("message", "Zombie kills data processed successfully");
            response.put("processed", processedCount);
            response.put("entries", processedEntries);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.severe("Error processing zombie kills file: " + e.getMessage());
            e.printStackTrace();

            response.put("success", false);
            response.put("error", "Error processing zombie kills: " + e.getMessage());
            response.put("processed", 0);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } finally {
            scheduleConsumeKills();
        }
    }

    /**
     * Processes a single zombie kills update entry
     */
    private Map<String, Object> processZombieKillsEntry(ZombieKillsUpdateDTO updateDTO) {
        Map<String, Object> entryResult = new HashMap<>();

        try {
            logger.info("Received zombie kills update for player: " + updateDTO.playerName() +
                    " (Steam ID: " + updateDTO.playerId() + ")");

            // Validate required fields
            if (updateDTO.playerId() == null || updateDTO.playerId().isEmpty()) {
                entryResult.put("success", false);
                entryResult.put("error", "Player ID (Steam ID) is required");
                entryResult.put("playerName", updateDTO.playerName());
                return entryResult;
            }

            if (updateDTO.playerName() == null || updateDTO.playerName().isEmpty()) {
                entryResult.put("success", false);
                entryResult.put("error", "Player name is required");
                entryResult.put("playerId", updateDTO.playerId());
                return entryResult;
            }

            // Find or create user by Steam ID
            // If user hasn't logged in yet, create a minimal entry to track their stats
            // Use approximate lookup to handle byte drift from Lua precision loss
            Optional<User> approximateUser = userService.getUserByApproximateSteamId(updateDTO.playerIdNumeric(), new BigDecimal(UserService.STEAM_ID_DEVIATION_TOLERANCE));
            
            User user = approximateUser.orElseGet(() -> 
                userService.createOrGetUserBySteamId(updateDTO.playerIdNumeric())
            );
            
            logger.info("Processing stats for user: " + user.getUsername() + " (Steam ID: " + user.getSteamId()
                    + ", Calculated: " + updateDTO.playerId() + ")");

            // Update character stats
            Character character = characterService.updateCharacterStats(user, updateDTO);

            // Get updated totals from database (avoids lazy loading issue)
            List<Character> userCharacters = characterService.getUserCharacters(user);
            int totalKills = userCharacters.stream()
                    .mapToInt(Character::getZombieKills)
                    .sum();
            int totalPoints = userCharacters.stream()
                    .mapToInt(Character::getCurrencyPoints)
                    .sum();

            // Build entry result
            entryResult.put("success", true);
            entryResult.put("message", "Character stats updated successfully");
            entryResult.put("playerName", character.getPlayerName());
            entryResult.put("characterId", character.getId());
            entryResult.put("zombieKills", character.getZombieKills());
            entryResult.put("currencyPoints", character.getCurrencyPoints());
            entryResult.put("userTotalKills", totalKills);
            entryResult.put("userTotalPoints", totalPoints);

            logger.info("Successfully updated character " + character.getPlayerName() +
                    " - Total kills: " + character.getZombieKills());

            return entryResult;

        } catch (Exception e) {
            logger.severe("Error processing zombie kills entry: " + e.getMessage());
            entryResult.put("success", false);
            entryResult.put("error", "Error: " + e.getMessage());
            entryResult.put("playerName", updateDTO.playerName());
            return entryResult;
        }
    }

    @GetMapping("/character/{characterId}")
    public ResponseEntity<Character> getCharacter(@PathVariable Long characterId) {
        return characterService.getCharacterById(characterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
