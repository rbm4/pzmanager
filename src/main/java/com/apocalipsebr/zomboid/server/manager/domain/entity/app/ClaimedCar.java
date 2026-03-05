package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claimed_cars")
public class ClaimedCar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_hash", nullable = false, unique = true)
    private String vehicleHash;

    @Column(name = "owner_steam_id", nullable = false)
    private String ownerSteamId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "vehicle_name")
    private String vehicleName;

    @Column(name = "script_name")
    private String scriptName;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    @Column(name = "last_updated")
    private Long lastUpdated;

    @Column(name = "migrated", nullable = false)
    private Boolean migrated = false;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "claimedCar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClaimedCarItem> items = new ArrayList<>();

    public ClaimedCar() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ClaimedCar(String vehicleHash, String ownerSteamId, String ownerName) {
        this();
        this.vehicleHash = vehicleHash;
        this.ownerSteamId = ownerSteamId;
        this.ownerName = ownerName;
    }

    // Helper to manage bidirectional relationship with items
    public void addItem(ClaimedCarItem item) {
        items.add(item);
        item.setClaimedCar(this);
    }

    public void removeItem(ClaimedCarItem item) {
        items.remove(item);
        item.setClaimedCar(null);
    }

    public void clearItems() {
        for (ClaimedCarItem item : new ArrayList<>(items)) {
            removeItem(item);
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleHash() {
        return vehicleHash;
    }

    public void setVehicleHash(String vehicleHash) {
        this.vehicleHash = vehicleHash;
    }

    public String getOwnerSteamId() {
        return ownerSteamId;
    }

    public void setOwnerSteamId(String ownerSteamId) {
        this.ownerSteamId = ownerSteamId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getMigrated() {
        return migrated;
    }

    public void setMigrated(Boolean migrated) {
        this.migrated = migrated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<ClaimedCarItem> getItems() {
        return items;
    }

    public void setItems(List<ClaimedCarItem> items) {
        this.items = items;
    }
}
