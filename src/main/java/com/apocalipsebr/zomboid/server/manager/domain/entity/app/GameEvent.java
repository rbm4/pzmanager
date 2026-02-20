package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game_events")
public class GameEvent {

    public enum EventStatus {
        PENDING,
        FUNDED,
        ACTIVE,
        EXPIRED,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.PENDING;

    @Column(name = "total_cost", nullable = false)
    private Integer totalCost;

    @Column(name = "amount_collected", nullable = false)
    private Integer amountCollected = 0;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays = 7;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gameEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GameEventProperty> properties = new ArrayList<>();

    @OneToMany(mappedBy = "gameEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GameEventContribution> contributions = new ArrayList<>();

    public GameEvent() {
        this.createdAt = LocalDateTime.now();
    }

    // ==================== Convenience Methods ====================

    public boolean isFullyFunded() {
        return amountCollected != null && totalCost != null && amountCollected >= totalCost;
    }

    public int getRemainingAmount() {
        if (totalCost == null || amountCollected == null) return 0;
        return Math.max(0, totalCost - amountCollected);
    }

    public double getFundingPercentage() {
        if (totalCost == null || totalCost == 0) return 100.0;
        return Math.min(100.0, (amountCollected * 100.0) / totalCost);
    }

    public boolean hasRegionProperties() {
        return properties.stream().anyMatch(p -> "REGION".equals(p.getPropertyTarget()));
    }

    public boolean hasSandboxProperties() {
        return properties.stream().anyMatch(p -> "SANDBOX".equals(p.getPropertyTarget()));
    }

    public void addContribution(GameEventContribution contribution) {
        contributions.add(contribution);
        contribution.setGameEvent(this);
    }

    public void addProperty(GameEventProperty property) {
        properties.add(property);
        property.setGameEvent(this);
    }

    // ==================== Getters and Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public Integer getTotalCost() { return totalCost; }
    public void setTotalCost(Integer totalCost) { this.totalCost = totalCost; }

    public Integer getAmountCollected() { return amountCollected; }
    public void setAmountCollected(Integer amountCollected) { this.amountCollected = amountCollected; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }

    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<GameEventProperty> getProperties() { return properties; }
    public void setProperties(List<GameEventProperty> properties) { this.properties = properties; }

    public List<GameEventContribution> getContributions() { return contributions; }
    public void setContributions(List<GameEventContribution> contributions) { this.contributions = contributions; }
}
