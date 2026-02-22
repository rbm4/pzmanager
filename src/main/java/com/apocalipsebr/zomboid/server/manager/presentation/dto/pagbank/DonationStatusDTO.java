package com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank;

/**
 * DTO for returning donation status to the frontend.
 */
public class DonationStatusDTO {
    private Long donationId;
    private String status; // PENDING, PAID, EXPIRED, FAILED
    private int amountCentavos;
    private int coinsAwarded;
    private String pixCopyPaste;
    private String qrCodeImageUrl;
    private String characterName;
    private long expiresInSeconds;

    public DonationStatusDTO() {}

    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getAmountCentavos() { return amountCentavos; }
    public void setAmountCentavos(int amountCentavos) { this.amountCentavos = amountCentavos; }
    public int getCoinsAwarded() { return coinsAwarded; }
    public void setCoinsAwarded(int coinsAwarded) { this.coinsAwarded = coinsAwarded; }
    public String getPixCopyPaste() { return pixCopyPaste; }
    public void setPixCopyPaste(String pixCopyPaste) { this.pixCopyPaste = pixCopyPaste; }
    public String getQrCodeImageUrl() { return qrCodeImageUrl; }
    public void setQrCodeImageUrl(String qrCodeImageUrl) { this.qrCodeImageUrl = qrCodeImageUrl; }
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}
