package com.apocalipsebr.zomboid.server.manager.presentation.dto;

public class ZombieKillsUpdateDTO {
    
    private String playerName;
    private String playerId; // Steam ID
    private Long timestamp;
    private String serverName;
    private Integer updateNumber;
    private String updateReason;
    
    private Integer killsSinceLastUpdate;
    private Integer totalSessionKills;
    
    private Integer x;
    private Integer y;
    private Integer z;
    
    private Integer health;
    private Boolean infected;
    private Boolean isDead;
    
    private Boolean inVehicle;
    private String profession;
    private Double hoursSurvived;

    // Getters and Setters
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Integer getUpdateNumber() {
        return updateNumber;
    }

    public void setUpdateNumber(Integer updateNumber) {
        this.updateNumber = updateNumber;
    }

    public String getUpdateReason() {
        return updateReason;
    }

    public void setUpdateReason(String updateReason) {
        this.updateReason = updateReason;
    }

    public Integer getKillsSinceLastUpdate() {
        return killsSinceLastUpdate;
    }

    public void setKillsSinceLastUpdate(Integer killsSinceLastUpdate) {
        this.killsSinceLastUpdate = killsSinceLastUpdate;
    }

    public Integer getTotalSessionKills() {
        return totalSessionKills;
    }

    public void setTotalSessionKills(Integer totalSessionKills) {
        this.totalSessionKills = totalSessionKills;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Boolean getInfected() {
        return infected;
    }

    public void setInfected(Boolean infected) {
        this.infected = infected;
    }

    public Boolean getIsDead() {
        return isDead;
    }

    public void setIsDead(Boolean isDead) {
        this.isDead = isDead;
    }

    public Boolean getInVehicle() {
        return inVehicle;
    }

    public void setInVehicle(Boolean inVehicle) {
        this.inVehicle = inVehicle;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public Double getHoursSurvived() {
        return hoursSurvived;
    }

    public void setHoursSurvived(Double hoursSurvived) {
        this.hoursSurvived = hoursSurvived;
    }
}
