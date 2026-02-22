package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "pagbank_order_id")
    private String pagbankOrderId;

    @Column(name = "amount_centavos", nullable = false)
    private Integer amountCentavos;

    @Column(name = "coins_awarded", nullable = false)
    private Integer coinsAwarded;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, PAID, EXPIRED, FAILED

    @Column(name = "pix_copy_paste", columnDefinition = "TEXT")
    private String pixCopyPaste;

    @Column(name = "qr_code_image_url", columnDefinition = "TEXT")
    private String qrCodeImageUrl;

    @Column(name = "expires_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    public Donation() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Character getCharacter() { return character; }
    public void setCharacter(Character character) { this.character = character; }

    public String getPagbankOrderId() { return pagbankOrderId; }
    public void setPagbankOrderId(String pagbankOrderId) { this.pagbankOrderId = pagbankOrderId; }

    public Integer getAmountCentavos() { return amountCentavos; }
    public void setAmountCentavos(Integer amountCentavos) { this.amountCentavos = amountCentavos; }

    public Integer getCoinsAwarded() { return coinsAwarded; }
    public void setCoinsAwarded(Integer coinsAwarded) { this.coinsAwarded = coinsAwarded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPixCopyPaste() { return pixCopyPaste; }
    public void setPixCopyPaste(String pixCopyPaste) { this.pixCopyPaste = pixCopyPaste; }

    public String getQrCodeImageUrl() { return qrCodeImageUrl; }
    public void setQrCodeImageUrl(String qrCodeImageUrl) { this.qrCodeImageUrl = qrCodeImageUrl; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
