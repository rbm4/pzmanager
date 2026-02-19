package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "transaction_type", nullable = false)
    private String transactionType; // "ITEM_PURCHASE", "CAR_PURCHASE", "CASHBACK"

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_id_ref")
    private String itemIdRef; // itemId for items, vehicleScript for cars

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "character_name", nullable = false)
    private String characterName;

    @Column(name = "player_username", nullable = false)
    private String playerUsername;

    @Column(name = "cashback", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean cashback = false;

    @Column(name = "cashback_at")
    private LocalDateTime cashbackAt;

    @Column(name = "cashback_by")
    private String cashbackBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public TransactionLog() {
        this.createdAt = LocalDateTime.now();
        this.cashback = false;
    }

    public TransactionLog(User user, Character character, String transactionType, 
                          String itemName, String itemIdRef, Integer amount, 
                          Integer balanceAfter) {
        this();
        this.user = user;
        this.character = character;
        this.transactionType = transactionType;
        this.itemName = itemName;
        this.itemIdRef = itemIdRef;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.characterName = character.getPlayerName();
        this.playerUsername = user.getUsername();
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

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemIdRef() {
        return itemIdRef;
    }

    public void setItemIdRef(String itemIdRef) {
        this.itemIdRef = itemIdRef;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public String getPlayerUsername() {
        return playerUsername;
    }

    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }

    public Boolean getCashback() {
        return cashback;
    }

    public void setCashback(Boolean cashback) {
        this.cashback = cashback;
    }

    public LocalDateTime getCashbackAt() {
        return cashbackAt;
    }

    public void setCashbackAt(LocalDateTime cashbackAt) {
        this.cashbackAt = cashbackAt;
    }

    public String getCashbackBy() {
        return cashbackBy;
    }

    public void setCashbackBy(String cashbackBy) {
        this.cashbackBy = cashbackBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
