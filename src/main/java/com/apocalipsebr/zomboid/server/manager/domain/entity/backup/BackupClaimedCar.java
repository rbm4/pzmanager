package com.apocalipsebr.zomboid.server.manager.domain.entity.backup;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "claimed_cars")
public class BackupClaimedCar {

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

    @Column(name = "preserved_for_migration", nullable = false)
    private Boolean preservedForMigration = false;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "claimedCar", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BackupClaimedCarItem> items = new ArrayList<>();

    // Getters
    public Long getId() {
        return id;
    }

    public String getVehicleHash() {
        return vehicleHash;
    }

    public String getOwnerSteamId() {
        return ownerSteamId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public Boolean getPreservedForMigration() {
        return preservedForMigration;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public List<BackupClaimedCarItem> getItems() {
        return items;
    }
}
