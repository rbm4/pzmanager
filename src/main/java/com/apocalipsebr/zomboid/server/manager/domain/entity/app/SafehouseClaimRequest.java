package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "safehouse_claim_requests")
public class SafehouseClaimRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SafehouseClaimStatus status = SafehouseClaimStatus.PENDING_REVIEW;

    @Column(name = "claim_name", nullable = false, length = 120)
    private String claimName;

    @Column(name = "x1", nullable = false)
    private Integer x1;

    @Column(name = "y1", nullable = false)
    private Integer y1;

    @Column(name = "x2", nullable = false)
    private Integer x2;

    @Column(name = "y2", nullable = false)
    private Integer y2;

    @Column(name = "cost", nullable = false)
    private Integer cost;

    @Column(name = "overlaps_existing", nullable = false)
    private Boolean overlapsExisting = false;

    @Column(name = "overlap_count", nullable = false)
    private Integer overlapCount = 0;

    @Column(name = "admin_reason", columnDefinition = "TEXT")
    private String adminReason;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SafehouseClaimRequest() {
        this.createdAt = LocalDateTime.now();
    }

    public SafehouseClaimRequest(User user, String claimName, int x1, int y1, int x2, int y2,
                                 int cost, boolean overlapsExisting, int overlapCount) {
        this();
        this.user = user;
        this.claimName = claimName;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cost = cost;
        this.overlapsExisting = overlapsExisting;
        this.overlapCount = overlapCount;
        this.status = SafehouseClaimStatus.PENDING_REVIEW;
    }

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

    public SafehouseClaimStatus getStatus() {
        return status;
    }

    public void setStatus(SafehouseClaimStatus status) {
        this.status = status;
    }

    public String getClaimName() {
        return claimName;
    }

    public void setClaimName(String claimName) {
        this.claimName = claimName;
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

    public Boolean getOverlapsExisting() {
        return overlapsExisting;
    }

    public void setOverlapsExisting(Boolean overlapsExisting) {
        this.overlapsExisting = overlapsExisting;
    }

    public Integer getOverlapCount() {
        return overlapCount;
    }

    public void setOverlapCount(Integer overlapCount) {
        this.overlapCount = overlapCount;
    }

    public String getAdminReason() {
        return adminReason;
    }

    public void setAdminReason(String adminReason) {
        this.adminReason = adminReason;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getArea() {
        return Math.abs(x2 - x1) * Math.abs(y2 - y1);
    }
}