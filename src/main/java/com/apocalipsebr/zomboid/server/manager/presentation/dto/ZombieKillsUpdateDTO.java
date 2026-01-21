package com.apocalipsebr.zomboid.server.manager.presentation.dto;

public record ZombieKillsUpdateDTO(
    String playerName,
    String playerId,
    Double playerIdNumeric,
    String timestamp,
    Double timestampNumeric,
    String serverName,
    Integer updateNumber,
    String updateReason,
    Integer killsSinceLastUpdate,
    Integer totalSessionKills,
    Integer x,
    Integer y,
    Integer z,
    Integer health,
    Boolean infected,
    Boolean isDead,
    Boolean inVehicle,
    String profession,
    Double hoursSurvived
){
    public ZombieKillsUpdateDTO withTimestampNumeric(Double newTimestampNumeric) {
        return new ZombieKillsUpdateDTO(
            playerName, playerId, playerIdNumeric, timestamp, newTimestampNumeric,
            serverName, updateNumber, updateReason, killsSinceLastUpdate, totalSessionKills,
            x, y, z, health, infected, isDead, inVehicle, profession, hoursSurvived
        );
    }

    public ZombieKillsUpdateDTO withPlayerIdNumeric(Double newPlayerIdNumeric) {
        return new ZombieKillsUpdateDTO(
            playerName, playerId, newPlayerIdNumeric, timestamp, timestampNumeric,
            serverName, updateNumber, updateReason, killsSinceLastUpdate, totalSessionKills,
            x, y, z, health, infected, isDead, inVehicle, profession, hoursSurvived
        );
    }
}
