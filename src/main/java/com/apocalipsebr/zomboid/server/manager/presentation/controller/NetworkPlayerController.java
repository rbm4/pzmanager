package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.NetworkPlayerService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.player.NetworkPlayer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/players")
@PreAuthorize("hasRole('ADMIN')")
public class NetworkPlayerController {
    
    private final NetworkPlayerService networkPlayerService;
    
    public NetworkPlayerController(NetworkPlayerService networkPlayerService) {
        this.networkPlayerService = networkPlayerService;
    }
    
    /**
     * Display the network players management page
     */
    @GetMapping
    public String showPlayersPage(Model model) {
        return "admin-players";
    }
    
    /**
     * API endpoint to get paginated and filtered players
     */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPlayers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<NetworkPlayer> playersPage;
        if (search != null && !search.trim().isEmpty()) {
            playersPage = networkPlayerService.searchPlayers(search, pageable);
        } else {
            playersPage = networkPlayerService.getAllPlayers(pageable);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("players", playersPage.getContent());
        response.put("currentPage", playersPage.getNumber());
        response.put("totalItems", playersPage.getTotalElements());
        response.put("totalPages", playersPage.getTotalPages());
        response.put("hasNext", playersPage.hasNext());
        response.put("hasPrevious", playersPage.hasPrevious());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to ban a player by Steam ID
     */
    @PostMapping("/api/ban")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> banPlayer(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String steamId = request.get("steamId");
            
            if (steamId == null || steamId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Steam ID is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            networkPlayerService.banPlayerBySteamId(steamId);
            
            response.put("success", true);
            response.put("message", "Player banned successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error banning player: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
