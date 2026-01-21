package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.PlayerStats;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.PlayerStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class PlayerStatsService {
    private static final Logger logger = Logger.getLogger(PlayerStatsService.class.getName());
    
    private static final int SKILL_JOURNAL_COST = 1000;
    private static final String SKILL_JOURNAL_ITEM = "Base.SkillRecoveryBoundJournal";
    
    private final PlayerStatsRepository playerStatsRepository;
    private final ServerCommandService serverCommandService;

    public PlayerStatsService(PlayerStatsRepository playerStatsRepository, 
                            ServerCommandService serverCommandService) {
        this.playerStatsRepository = playerStatsRepository;
        this.serverCommandService = serverCommandService;
    }

    public PlayerStats getPlayerStats(String username) {
        return playerStatsRepository.findByUsername(username)
            .orElseGet(() -> {
                PlayerStats newStats = new PlayerStats(username);
                return playerStatsRepository.save(newStats);
            });
    }

    @Transactional
    public boolean purchaseSkillJournal(String username) {
        try {
            Optional<PlayerStats> statsOpt = playerStatsRepository.findByUsername(username);
            
            if (statsOpt.isEmpty()) {
                logger.warning("Player stats not found for: " + username);
                return false;
            }
            
            PlayerStats stats = statsOpt.get();
            
            if (stats.getCurrencyPoints() < SKILL_JOURNAL_COST) {
                logger.warning("Insufficient currency points for " + username);
                return false;
            }
            
            // Deduct currency points
            stats.setCurrencyPoints(stats.getCurrencyPoints() - SKILL_JOURNAL_COST);
            playerStatsRepository.save(stats);
            
            // Give item via RCON
            String command = String.format("additem \"%s\" \"%s\" 1", username, SKILL_JOURNAL_ITEM);
            serverCommandService.sendCommand(command);
            
            logger.info("Player " + username + " purchased Skill Recovery Journal");
            return true;
            
        } catch (Exception e) {
            logger.severe("Failed to process purchase for " + username + ": " + e.getMessage());
            return false;
        }
    }

    public int getSkillJournalCost() {
        return SKILL_JOURNAL_COST;
    }

    public PlayerStats save(PlayerStats playerStats) {
        return playerStatsRepository.save(playerStats);
    }
}
