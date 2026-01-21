package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.PlayerStats;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class UserService {

    public static final int STEAM_ID_DEVIATION_TOLERANCE = 10;

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    private final UserRepository userRepository;
    private final PlayerStatsService playerStatsService;

    public UserService(UserRepository userRepository, PlayerStatsService playerStatsService) {
        this.userRepository = userRepository;
        this.playerStatsService = playerStatsService;
    }

    /**
     * Creates a minimal user with only Steam ID if not found, or returns existing
     * user.
     * This is used when tracking stats for users who haven't logged in yet.
     */
    @Transactional
    public User createOrGetUserBySteamId(BigDecimal approximateSteamID) {
        var minSteamId = approximateSteamID.subtract(new BigDecimal(STEAM_ID_DEVIATION_TOLERANCE));
        var maxSteamId = approximateSteamID.add(new BigDecimal(STEAM_ID_DEVIATION_TOLERANCE));
        Optional<User> existingUser = userRepository.findByApproximateSteamId(minSteamId.longValue(),
                maxSteamId.longValue());

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create minimal user with only steamId and placeholder username
        User newUser = new User(approximateSteamID.toString(), "Player_" + approximateSteamID);
        newUser.setLastLogin(null); // Not logged in yet

        logger.info("Created minimal user entry for Steam ID: " + approximateSteamID);
        return userRepository.save(newUser);
    }

    @Transactional
    public User processOAuthUser(String steamId, String username, String avatarUrl, String profileUrl) {
        Optional<User> existingUser = userRepository.findBySteamId(steamId);

        // If no exact match, try approximate match (for users created from Lua with
        // imprecise Steam IDs)
        if (existingUser.isEmpty()) {
            long steamIdLong = Long.parseLong(steamId);
            long minSteamId = steamIdLong - STEAM_ID_DEVIATION_TOLERANCE;
            long maxSteamId = steamIdLong + STEAM_ID_DEVIATION_TOLERANCE;
            existingUser = userRepository.findByApproximateSteamId(minSteamId, maxSteamId);

            if (existingUser.isPresent()) {
                // Update the approximate Steam ID to the correct one from OAuth
                User user = existingUser.get();
                logger.info("Found approximate Steam ID match. Updating from " + user.getSteamId() + " to " + steamId);
                user.setSteamId(steamId);
            }
        }

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

            // If this was a minimal user (created from zombie kills), now fully initialize
            // it
            if (user.getPlayerStats() == null) {
                PlayerStats playerStats = playerStatsService.getPlayerStats(username);
                user.setPlayerStats(playerStats);
                logger.info("Updated minimal user to full profile: " + username + " (" + steamId + ")");
            } else {
                logger.info("Updated existing user: " + username + " (" + steamId + ")");
            }

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

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by approximate Steam ID within a tolerance range of ±10.
     * Used to handle byte drift from imprecise Steam ID calculations in Lua.
     * Performs the lookup at the database layer for efficiency.
     */
    public Optional<User> getUserByApproximateSteamId(BigDecimal approximateSteamId, BigDecimal tolerance) {
        var minSteamId = approximateSteamId.subtract(tolerance);
        var maxSteamId = approximateSteamId.add(tolerance);

        Optional<User> user = userRepository.findByApproximateSteamId(minSteamId.longValue(), maxSteamId.longValue());

        if (user.isPresent()) {
            long userSteamId = Long.parseLong(user.get().getSteamId());
            long difference = Math.abs(userSteamId - approximateSteamId.longValue());
            logger.info("Found user by approximate Steam ID. Calculated: " + approximateSteamId +
                    ", Actual: " + userSteamId + ", Difference: " + difference);
        }

        return user;
    }

    @Transactional
    public User updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        logger.info("Updated role for user " + user.getUsername() + " to: " + role);
        return userRepository.save(user);
    }
}
