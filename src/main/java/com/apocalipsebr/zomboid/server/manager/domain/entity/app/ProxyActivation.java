package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proxy_activation")
public class ProxyActivation {

    public static final String STATUS_STARTING = "STARTING";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_STOPPING = "STOPPING";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "proxy_id", nullable = false, length = 50)
    private String proxyId;

    @Column(name = "instance_id", nullable = false, length = 50)
    private String instanceId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "credits_spent", nullable = false)
    private Integer creditsSpent;

    @Column(name = "hours", nullable = false)
    private Integer hours;

    @Column(name = "activated_at", nullable = false)
    private LocalDateTime activatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "stopped_at")
    private LocalDateTime stoppedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProxyActivation() {
        this.createdAt = LocalDateTime.now();
        this.activatedAt = LocalDateTime.now();
        this.status = STATUS_STARTING;
    }

    public ProxyActivation(User user, String proxyId, String instanceId, int creditsSpent, int hours) {
        this();
        this.user = user;
        this.proxyId = proxyId;
        this.instanceId = instanceId;
        this.creditsSpent = creditsSpent;
        this.hours = hours;
        this.expiresAt = this.activatedAt.plusHours(hours);
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getProxyId() { return proxyId; }
    public void setProxyId(String proxyId) { this.proxyId = proxyId; }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCreditsSpent() { return creditsSpent; }
    public void setCreditsSpent(Integer creditsSpent) { this.creditsSpent = creditsSpent; }

    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }

    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getStoppedAt() { return stoppedAt; }
    public void setStoppedAt(LocalDateTime stoppedAt) { this.stoppedAt = stoppedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
