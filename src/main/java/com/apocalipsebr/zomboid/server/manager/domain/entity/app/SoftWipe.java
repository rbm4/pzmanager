package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "soft_wipes")
public class SoftWipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SoftWipeStatus status = SoftWipeStatus.WAITING_RESTART;

    @Column(nullable = false)
    private Integer x1;

    @Column(nullable = false)
    private Integer y1;

    @Column(nullable = false)
    private Integer x2;

    @Column(nullable = false)
    private Integer y2;

    @Column(nullable = false)
    private Integer cost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "bins_deleted")
    private Integer binsDeleted = 0;

    @Column(name = "bins_protected")
    private Integer binsProtected = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public SoftWipe() {
        this.createdAt = LocalDateTime.now();
    }

    public SoftWipe(User user, int x1, int y1, int x2, int y2, int cost) {
        this.user = user;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cost = cost;
        this.status = SoftWipeStatus.WAITING_RESTART;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

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

    public SoftWipeStatus getStatus() {
        return status;
    }

    public void setStatus(SoftWipeStatus status) {
        this.status = status;
    }

    public Integer getX1() {
        return x1;
    }

    public void setX1(Integer x1) {
        this.x1 = x1;
    }

    public Integer getY1() {
        return y1;
    }

    public void setY1(Integer y1) {
        this.y1 = y1;
    }

    public Integer getX2() {
        return x2;
    }

    public void setX2(Integer x2) {
        this.x2 = x2;
    }

    public Integer getY2() {
        return y2;
    }

    public void setY2(Integer y2) {
        this.y2 = y2;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }

    public Integer getBinsDeleted() {
        return binsDeleted;
    }

    public void setBinsDeleted(Integer binsDeleted) {
        this.binsDeleted = binsDeleted;
    }

    public Integer getBinsProtected() {
        return binsProtected;
    }

    public void setBinsProtected(Integer binsProtected) {
        this.binsProtected = binsProtected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns the area of the selected rectangle in tile² units.
     */
    public int getArea() {
        return Math.abs(x2 - x1) * Math.abs(y2 - y1);
    }
}
