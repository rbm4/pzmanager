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
    
    private static final Logger logger = Logger.getLogger(CharacterService.class.getName());
    
    private final CharacterRepository characterRepository;
    
    public CharacterService(CharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }
    
    @Transactional
    public Character updateCharacterStats(User user, ZombieKillsUpdateDTO dto) {
        logger.info("Updating stats for character: " + dto.getPlayerName() + " (User: " + user.getUsername() + ")");
        
        Optional<Character> existingCharacter = characterRepository.findByUserAndPlayerName(user, dto.getPlayerName());
        
        Character character;
        if (existingCharacter.isPresent()) {
            character = existingCharacter.get();
            logger.info("Updating existing character: " + dto.getPlayerName());
        } else {
            character = new Character(user, dto.getPlayerName(), dto.getServerName());
            logger.info("Creating new character: " + dto.getPlayerName());
        }
        
        // Update character stats
        if (dto.getKillsSinceLastUpdate() != null && dto.getKillsSinceLastUpdate() > 0) {
            character.setZombieKills(character.getZombieKills() + dto.getKillsSinceLastUpdate());
            
            // Award currency points based on kills (1 point per kill)
            character.setCurrencyPoints(character.getCurrencyPoints() + dto.getKillsSinceLastUpdate());
            
            logger.info("Added " + dto.getKillsSinceLastUpdate() + " kills. Total: " + character.getZombieKills());
        }
        
        // Update location
        character.setLastX(dto.getX());
        character.setLastY(dto.getY());
        character.setLastZ(dto.getZ());
        
        // Update status
        character.setLastHealth(dto.getHealth());
        character.setLastInfected(dto.getInfected());
        character.setLastInVehicle(dto.getInVehicle());
        character.setIsDead(dto.getIsDead());
        
        // Update profession and hours survived
        if (dto.getProfession() != null) {
            character.setProfession(dto.getProfession());
        }
        if (dto.getHoursSurvived() != null) {
            character.setHoursSurvived(dto.getHoursSurvived());
        }
        
        // Update server name if changed
        if (dto.getServerName() != null) {
            character.setServerName(dto.getServerName());
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
    
    public List<Character> getActiveCharacters() {
        return characterRepository.findByIsDeadFalse();
    }
    
    public Optional<Character> getCharacterById(Long id) {
        return characterRepository.findById(id);
    }
    
    @Transactional
    public Character updateCharacterDeath(Long characterId, boolean isDead) {
        Character character = characterRepository.findById(characterId)
            .orElseThrow(() -> new IllegalArgumentException("Character not found"));
        character.setIsDead(isDead);
        return characterRepository.save(character);
    }
}
