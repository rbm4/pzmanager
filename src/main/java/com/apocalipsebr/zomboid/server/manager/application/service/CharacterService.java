package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterRepository;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.ZombieKillsUpdateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class CharacterService {
    
    private static final int CURRENCY_MULTIPLIER = 6;

    private static final Logger logger = Logger.getLogger(CharacterService.class.getName());
    
    private final CharacterRepository characterRepository;
    private final ServerCommandService serverCommandService;
    
    public CharacterService(CharacterRepository characterRepository, ServerCommandService serverCommandService) {
        this.characterRepository = characterRepository;
        this.serverCommandService = serverCommandService;
    }
    
    @Transactional
    public Character updateCharacterStats(User user, ZombieKillsUpdateDTO dto) {
        logger.info("Updating stats for character: " + dto.playerName() + " (User: " + user.getUsername() + ")");
        
        Optional<Character> existingCharacter = characterRepository.findByUserAndPlayerName(user, dto.playerName());
        
        Character character;
        if (existingCharacter.isPresent()) {
            character = existingCharacter.get();
            logger.info("Updating existing character: " + dto.playerName());
        } else {
            character = new Character(user, dto.playerName(), dto.serverName());
            logger.info("Creating new character: " + dto.playerName());
        }
        
        // Update character stats
        if (dto.killsSinceLastUpdate() != null && dto.killsSinceLastUpdate() > 0) {
            character.setZombieKills(character.getZombieKills() + dto.killsSinceLastUpdate());
            
            // Award currency points based on kills (1 point per kill)
            character.setCurrencyPoints(character.getCurrencyPoints() + (dto.killsSinceLastUpdate()*CURRENCY_MULTIPLIER));
            
            logger.info("Added " + dto.killsSinceLastUpdate() + " kills. Total: " + character.getZombieKills());
        }
        
        // Update location
        character.setLastX(dto.x());
        character.setLastY(dto.y());
        character.setLastZ(dto.z());
        
        // Update status
        character.setLastHealth(dto.health());
        character.setLastInfected(dto.infected());
        character.setLastInVehicle(dto.inVehicle());
        character.setIsDead(dto.isDead());
        
        // Update profession and hours survived
        if (dto.profession() != null) {
            character.setProfession(dto.profession());
        }
        if (dto.hoursSurvived() != null) {
            character.setHoursSurvived(dto.hoursSurvived());
        }
        
        // Update server name if changed
        if (dto.serverName() != null) {
            character.setServerName(dto.serverName());
        }
        
        character.setLastUpdate(LocalDateTime.now());
        
        return characterRepository.saveAndFlush(character);
    }
    
    public List<Character> getUserCharacters(User user) {
        return characterRepository.findByUserOrderByZombieKillsDesc(user);
    }
    
    public List<Character> getTopCharactersByKills() {
        return characterRepository.findTopActiveCharactersByKills();
    }
    
    public List<Character> getTopCharactersByHoursSurvived() {
        return characterRepository.findTopActiveCharactersByHoursSurvived();
    }
    
    public List<Character> getActiveCharacters() {
        return characterRepository.findByIsDeadFalse();
    }
    
    public Optional<Character> getCharacterById(Long id) {
        return characterRepository.findById(id);
    }

    public void saveAll(List<Character> characers){
        characterRepository.saveAll(characers);
    }
    
    @Transactional
    public Character updateCharacterDeath(Long characterId, boolean isDead) {
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new IllegalArgumentException("Character not found"));
        character.setIsDead(isDead);
        return characterRepository.save(character);
    }

    /**
     * Resets the user's PZ server password by removing them from the whitelist.
     * Validates that the given character belongs to the requesting user.
     * The user will be prompted for a new password on next login.
     *
     * @param user        the authenticated user requesting the reset
     * @param characterId the character ID used to validate ownership
     * @return true if the command was sent successfully
     * @throws IllegalArgumentException if the character is not found or doesn't belong to the user
     */
    public boolean resetUserPassword(User user, Long characterId) {
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new IllegalArgumentException("Character not found"));

        if (!character.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Character does not belong to the requesting user");
        }

        String username = user.getUsername();
        logger.info("Resetting password for user '" + username + "' (requested via character '" + character.getPlayerName() + "')");

        serverCommandService.sendCommand("removeuserfromwhitelist \"" + username + "\"");
        logger.info("Successfully sent removeuserfromwhitelist command for user '" + username + "'");
        return true;
    }
}
