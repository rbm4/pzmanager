package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ServerCommandService;
import com.apocalipsebr.zomboid.server.manager.application.service.ServerRestartService;
import com.apocalipsebr.zomboid.server.manager.application.service.ScheduledRestartService;
import com.apocalipsebr.zomboid.server.manager.application.service.ServerWipeService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/server")
public class ServerController {
    private final ServerCommandService serverCommandService;
    private final ServerRestartService serverRestartService;
    private final ScheduledRestartService scheduledRestartService;
    private final ServerWipeService serverWipeService;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    public ServerController(ServerCommandService serverCommandService, 
                          ServerRestartService serverRestartService,
                          ScheduledRestartService scheduledRestartService,
                          ServerWipeService serverWipeService,
                          CharacterRepository characterRepository,
                          UserRepository userRepository) {
        this.serverCommandService = serverCommandService;
        this.serverRestartService = serverRestartService;
        this.scheduledRestartService = scheduledRestartService;
        this.serverWipeService = serverWipeService;
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/command")
    public ResponseEntity<Map<String, Object>> sendCommand(@RequestBody CommandRequest request) {
        try {
            String result = serverCommandService.sendCommandResponse(request.getCommand());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "command", request.getCommand(),
                "response", result != null && !result.isEmpty() ? result : "(empty response)",
                "message", "Command executed successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "command", request.getCommand(),
                    "error", e.getMessage()
                ));
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/command/text")
    public ResponseEntity<String> sendCommandAsText(@RequestParam String command) {
        serverCommandService.sendCommand(command);
        return ResponseEntity.ok("Command sent successfully: " + command);
    }

    @PostMapping("/restart")
    public ResponseEntity<String> restartServer(@RequestBody RestartRequest request) {
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Password is required");
        }

        if (!serverRestartService.validatePassword(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid password");
        }

        if (serverRestartService.isRestartInProgress()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A restart is already in progress");
        }

        try {
            serverRestartService.initiateRestart();
            return ResponseEntity.ok("Server restart initiated. Warnings will be sent at 10, 5, 1 minute(s) and 15 seconds before restart.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to initiate restart: " + e.getMessage());
        }
    }

    @GetMapping("/restart/scheduled")
    public ResponseEntity<Map<String, Object>> getScheduledRestartInfo() {
        if (!scheduledRestartService.isScheduledRestartEnabled()) {
            return ResponseEntity.ok(Map.of(
                "enabled", false,
                "message", "Scheduled automatic restart is disabled"
            ));
        }

        LocalDateTime nextRestart = scheduledRestartService.getNextScheduledRestart();
        return ResponseEntity.ok(Map.of(
            "enabled", true,
            "nextRestart", nextRestart.toString(),
            "message", "Next automatic restart scheduled for: " + nextRestart
        ));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add-currency")
    public ResponseEntity<Map<String, Object>> addCurrency(@RequestBody AddCurrencyRequest request) {
        try {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Usuário não encontrado: " + request.getUsername()
                    ));
            }

            User user = userOpt.get();
            List<Character> userCharacters = characterRepository.findByUserOrderByZombieKillsDesc(user);
            
            if (userCharacters.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", "Nenhum personagem encontrado para o usuário: " + request.getUsername()
                    ));
            }

            // Get character with most zombie kills (already ordered by kills desc)
            Character topCharacter = userCharacters.get(0);
            
            // Add currency
            int currentCurrency = topCharacter.getCurrencyPoints() != null ? topCharacter.getCurrencyPoints() : 0;
            int newCurrency = currentCurrency + request.getAmount();
            topCharacter.setCurrencyPoints(newCurrency);
            characterRepository.save(topCharacter);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Moeda adicionada com sucesso!",
                "character", topCharacter.getPlayerName(),
                "zombieKills", topCharacter.getZombieKills(),
                "previousCurrency", currentCurrency,
                "addedAmount", request.getAmount(),
                "newCurrency", newCurrency
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao adicionar moeda: " + e.getMessage()
                ));
        }
    }

    public static class RestartRequest {
        private String password;

        public RestartRequest() {
        }

        public RestartRequest(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class CommandRequest {
        private String command;

        public CommandRequest() {
        }

        public CommandRequest(String command) {
            this.command = command;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }

    public static class AddCurrencyRequest {
        private String username;
        private int amount;

        public AddCurrencyRequest() {
        }

        public AddCurrencyRequest(String username, int amount) {
            this.username = username;
            this.amount = amount;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/wipe")
    public ResponseEntity<Map<String, Object>> wipeServer(@RequestBody WipeRequest request) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", "Password is required."));
        }

        if (!serverWipeService.validatePassword(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Invalid wipe password."));
        }

        ServerWipeService.WipeResult result = serverWipeService.wipeServer();

        if (result.isSuccess()) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result.getMessage(),
                "backupName", result.getBackupName(),
                "originalPath", result.getOriginalPath(),
                "backupPath", result.getBackupPath()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", result.getMessage()));
        }
    }

    public static class WipeRequest {
        private String password;

        public WipeRequest() {
        }

        public WipeRequest(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
