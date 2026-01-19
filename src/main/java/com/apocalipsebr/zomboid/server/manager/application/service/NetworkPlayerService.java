package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.player.NetworkPlayer;
import com.apocalipsebr.zomboid.server.manager.domain.repository.player.NetworkPlayerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NetworkPlayerService {
    
    private final NetworkPlayerRepository networkPlayerRepository;
    private final ServerCommandService serverCommandService;
    
    public NetworkPlayerService(NetworkPlayerRepository networkPlayerRepository, 
                               ServerCommandService serverCommandService) {
        this.networkPlayerRepository = networkPlayerRepository;
        this.serverCommandService = serverCommandService;
    }
    
    /**
     * Get all players with pagination
     */
    public Page<NetworkPlayer> getAllPlayers(Pageable pageable) {
        return networkPlayerRepository.findAll(pageable);
    }
    
    /**
     * Search players by name or Steam ID with pagination
     */
    public Page<NetworkPlayer> searchPlayers(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return getAllPlayers(pageable);
        }
        return networkPlayerRepository.searchByNameOrSteamId(search.trim(), pageable);
    }
    
    /**
     * Ban a player by Steam ID using the server command
     */
    public void banPlayerBySteamId(String steamId) {
        if (steamId == null || steamId.trim().isEmpty()) {
            throw new IllegalArgumentException("Steam ID cannot be empty");
        }
        
        String command = String.format("/banid \"%s\"", steamId.trim());
        serverCommandService.sendCommand(command);
    }
}
