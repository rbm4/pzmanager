package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.PlayerStats;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class UserService {
    
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    
    private final UserRepository userRepository;
    private final PlayerStatsService playerStatsService;

    public UserService(UserRepository userRepository, PlayerStatsService playerStatsService) {
        this.userRepository = userRepository;
        this.playerStatsService = playerStatsService;
    }

    @Transactional
    public User processOAuthUser(String steamId, String username, String avatarUrl, String profileUrl) {
        Optional<User> existingUser = userRepository.findBySteamId(steamId);
        
        // Check if this is the admin Steam ID
        boolean isAdmin = "76561198394820414".equals(steamId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLogin(LocalDateTime.now());
            user.setUsername(username);
            user.setAvatarUrl(avatarUrl);
            user.setProfileUrl(profileUrl);
            
            // Always ensure admin role for the designated Steam ID
            if (isAdmin && !"ADMIN".equals(user.getRole())) {
                user.setRole("ADMIN");
                logger.info("Assigned ADMIN role to user: " + username + " (" + steamId + ")");
            }
            
            logger.info("Updated existing user: " + username + " (" + steamId + ")");
            return userRepository.save(user);
        } else {
            // Create PlayerStats for new user first
            PlayerStats playerStats = playerStatsService.getPlayerStats(username);
            
            // Create new user
            User newUser = new User(steamId, username);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setProfileUrl(profileUrl);
            newUser.setLastLogin(LocalDateTime.now());
            
            // Set admin role if this is the designated Steam ID
            if (isAdmin) {
                newUser.setRole("ADMIN");
                logger.info("Created new ADMIN user: " + username + " (" + steamId + ")");
            } else {
                logger.info("Created new user: " + username + " (" + steamId + ")");
            }
            
            // Save user first without PlayerStats relationship
            newUser = userRepository.save(newUser);
            
            // Now set the PlayerStats and update
            newUser.setPlayerStats(playerStats);
            
            return userRepository.save(newUser);
        }
    }

    public Optional<User> getUserBySteamId(String steamId) {
        return userRepository.findBySteamId(steamId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
