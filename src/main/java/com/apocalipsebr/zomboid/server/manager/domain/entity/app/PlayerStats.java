package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;

@Entity
@Table(name = "player_stats")
public class PlayerStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    
    @Column(name = "zombie_kills")
    private Integer zombieKills = 0;
    
    @Column(name = "money")
    private Integer money = 0;
    
    @Column(name = "currency_points")
    private Integer currencyPoints = 0;

    public PlayerStats() {
    }

    public PlayerStats(String username) {
        this.username = username;
        this.zombieKills = 0;
        this.money = 0;
        this.currencyPoints = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getZombieKills() {
        return zombieKills;
    }

    public void setZombieKills(Integer zombieKills) {
        this.zombieKills = zombieKills;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getCurrencyPoints() {
        return currencyPoints;
    }

    public void setCurrencyPoints(Integer currencyPoints) {
        this.currencyPoints = currencyPoints;
    }
}
