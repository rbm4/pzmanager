package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "steam_id", unique = true, nullable = false)
    private String steamId;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "profile_url")
    private String profileUrl;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @OneToOne
    @JoinColumn(name = "player_stats_id", referencedColumnName = "id")
    private PlayerStats playerStats;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Character> characters = new ArrayList<>();
    
    @Column(name = "role")
    private String role = "PLAYER";

    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String steamId, String username) {
        this();
        this.steamId = steamId;
        this.username = username;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }

    public void setPlayerStats(PlayerStats playerStats) {
        this.playerStats = playerStats;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public List<Character> getCharacters() {
        return characters;
    }
    
    public void setCharacters(List<Character> characters) {
        this.characters = characters;
    }
    
    // Aggregate methods to calculate total kills and points from all characters
    public int getTotalZombieKills() {
        return characters.stream()
            .mapToInt(Character::getZombieKills)
            .sum();
    }
    
    public int getTotalCurrencyPoints() {
        return characters.stream()
            .mapToInt(Character::getCurrencyPoints)
            .sum();
    }
    
    public long getActiveCharactersCount() {
        return characters.stream()
            .filter(c -> !c.getIsDead())
            .count();
    }
}
