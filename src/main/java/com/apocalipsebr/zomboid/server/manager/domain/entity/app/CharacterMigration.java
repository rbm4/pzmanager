package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "character_migrations")
public class CharacterMigration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false)
    private String status = MigrationStatus.PENDING.name();

    @Column(name = "snapshot_skills", columnDefinition = "TEXT")
    private String snapshotSkills;

    @Column(name = "applied_skills", columnDefinition = "TEXT")
    private String appliedSkills;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "processed_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime processedAt;

    public CharacterMigration() {
        this.createdAt = LocalDateTime.now();
    }

    public CharacterMigration(Character character, User user, String snapshotSkills) {
        this();
        this.character = character;
        this.user = user;
        this.snapshotSkills = snapshotSkills;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MigrationStatus getMigrationStatus() {
        return MigrationStatus.valueOf(this.status);
    }

    public void setMigrationStatus(MigrationStatus status) {
        this.status = status.name();
    }

    public String getSnapshotSkills() {
        return snapshotSkills;
    }

    public void setSnapshotSkills(String snapshotSkills) {
        this.snapshotSkills = snapshotSkills;
    }

    public String getAppliedSkills() {
        return appliedSkills;
    }

    public void setAppliedSkills(String appliedSkills) {
        this.appliedSkills = appliedSkills;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
