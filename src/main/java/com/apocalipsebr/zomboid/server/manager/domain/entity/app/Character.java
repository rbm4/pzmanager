package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "characters")
public class Character {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "player_name", nullable = false)
    private String playerName;
    
    @Column(name = "server_name")
    private String serverName;
    
    @Column(name = "profession")
    private String profession;
    
    @Column(name = "zombie_kills")
    private Integer zombieKills = 0;
    
    @Column(name = "currency_points")
    private Integer currencyPoints = 0;
    
    @Column(name = "hours_survived")
    private Double hoursSurvived = 0.0;
    
    @Column(name = "is_dead")
    private Boolean isDead = false;
    
    @Column(name = "last_x")
    private Integer lastX;
    
    @Column(name = "last_y")
    private Integer lastY;
    
    @Column(name = "last_z")
    private Integer lastZ;
    
    @Column(name = "last_health")
    private Integer lastHealth;
    
    @Column(name = "last_infected")
    private Boolean lastInfected = false;
    
    @Column(name = "last_in_vehicle")
    private Boolean lastInVehicle = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    public Character() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    public Character(User user, String playerName, String serverName) {
        this();
        this.user = user;
        this.playerName = playerName;
        this.serverName = serverName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public Integer getZombieKills() {
        return zombieKills;
    }

    public void setZombieKills(Integer zombieKills) {
        this.zombieKills = zombieKills;
    }

    public Integer getCurrencyPoints() {
        return currencyPoints;
    }

    public void setCurrencyPoints(Integer currencyPoints) {
        this.currencyPoints = currencyPoints;
    }

    public Double getHoursSurvived() {
        return hoursSurvived;
    }

    public void setHoursSurvived(Double hoursSurvived) {
        this.hoursSurvived = hoursSurvived;
    }

    public Boolean getIsDead() {
        return isDead;
    }

    public void setIsDead(Boolean isDead) {
        this.isDead = isDead;
    }

    public Integer getLastX() {
        return lastX;
    }

    public void setLastX(Integer lastX) {
        this.lastX = lastX;
    }

    public Integer getLastY() {
        return lastY;
    }

    public void setLastY(Integer lastY) {
        this.lastY = lastY;
    }

    public Integer getLastZ() {
        return lastZ;
    }

    public void setLastZ(Integer lastZ) {
        this.lastZ = lastZ;
    }

    public Integer getLastHealth() {
        return lastHealth;
    }

    public void setLastHealth(Integer lastHealth) {
        this.lastHealth = lastHealth;
    }

    public Boolean getLastInfected() {
        return lastInfected;
    }

    public void setLastInfected(Boolean lastInfected) {
        this.lastInfected = lastInfected;
    }

    public Boolean getLastInVehicle() {
        return lastInVehicle;
    }

    public void setLastInVehicle(Boolean lastInVehicle) {
        this.lastInVehicle = lastInVehicle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
