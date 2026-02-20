package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_event_contributions")
public class GameEventContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_event_id", nullable = false)
    private GameEvent gameEvent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "contributed_at", nullable = false)
    private LocalDateTime contributedAt;

    public GameEventContribution() {
        this.contributedAt = LocalDateTime.now();
    }

    public GameEventContribution(User user, Integer amount) {
        this();
        this.user = user;
        this.amount = amount;
    }

    // ==================== Getters and Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GameEvent getGameEvent() { return gameEvent; }
    public void setGameEvent(GameEvent gameEvent) { this.gameEvent = gameEvent; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public LocalDateTime getContributedAt() { return contributedAt; }
    public void setContributedAt(LocalDateTime contributedAt) { this.contributedAt = contributedAt; }
}
