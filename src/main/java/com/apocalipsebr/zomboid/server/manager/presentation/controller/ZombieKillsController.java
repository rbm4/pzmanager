package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.UserService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.ZombieKillsUpdateDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/zombie-kills")
public class ZombieKillsController {
    
    private static final Logger logger = Logger.getLogger(ZombieKillsController.class.getName());
    
    private final UserService userService;
    private final CharacterService characterService;
    
    public ZombieKillsController(UserService userService, CharacterService characterService) {
        this.userService = userService;
        this.characterService = characterService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> updateZombieKills(@RequestBody ZombieKillsUpdateDTO updateDTO) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Received zombie kills update for player: " + updateDTO.getPlayerName() + 
                       " (Steam ID: " + updateDTO.getPlayerId() + ")");
            
            // Validate required fields
            if (updateDTO.getPlayerId() == null || updateDTO.getPlayerId().isEmpty()) {
                response.put("success", false);
                response.put("error", "Player ID (Steam ID) is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (updateDTO.getPlayerName() == null || updateDTO.getPlayerName().isEmpty()) {
                response.put("success", false);
                response.put("error", "Player name is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Find or create user by Steam ID
            // If user hasn't logged in yet, create a minimal entry to track their stats
            User user = userService.createOrGetUserBySteamId(updateDTO.getPlayerId());
            logger.info("Processing stats for user: " + user.getUsername() + " (Steam ID: " + updateDTO.getPlayerId() + ")");
            
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
            
            // Build response
            response.put("success", true);
            response.put("message", "Character stats updated successfully");
            response.put("characterId", character.getId());
            response.put("totalKills", character.getZombieKills());
            response.put("currencyPoints", character.getCurrencyPoints());
            response.put("userTotalKills", totalKills);
            response.put("userTotalPoints", totalPoints);
            
            logger.info("Successfully updated character " + character.getPlayerName() + 
                       " - Total kills: " + character.getZombieKills());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Error updating zombie kills: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/character/{characterId}")
    public ResponseEntity<Character> getCharacter(@PathVariable Long characterId) {
        return characterService.getCharacterById(characterId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
