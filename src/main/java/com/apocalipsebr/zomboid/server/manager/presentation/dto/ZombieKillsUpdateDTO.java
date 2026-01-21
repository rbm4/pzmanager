package com.apocalipsebr.zomboid.server.manager.presentation.dto;

import java.math.BigDecimal;

public record ZombieKillsUpdateDTO(
    String playerName,
    String playerId,
    BigDecimal playerIdNumeric,
    String timestamp,
    BigDecimal timestampNumeric,
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
    Double hoursSurvived,
    String playerIdHigh,
    String playerIdLow
){
    public ZombieKillsUpdateDTO withTimestampNumeric(BigDecimal newTimestampNumeric) {
        return new ZombieKillsUpdateDTO(
            playerName, playerId, playerIdNumeric, timestamp, newTimestampNumeric,
            serverName, updateNumber, updateReason, killsSinceLastUpdate, totalSessionKills,
            x, y, z, health, infected, isDead, inVehicle, profession, hoursSurvived,playerIdHigh,playerIdLow
        );
    }

    public ZombieKillsUpdateDTO withPlayerIdNumeric(BigDecimal newPlayerIdNumeric) {
        return new ZombieKillsUpdateDTO(
            playerName, playerId, newPlayerIdNumeric, timestamp, timestampNumeric,
            serverName, updateNumber, updateReason, killsSinceLastUpdate, totalSessionKills,
            x, y, z, health, infected, isDead, inVehicle, profession, hoursSurvived,playerIdHigh,playerIdLow
        );
    }
}
