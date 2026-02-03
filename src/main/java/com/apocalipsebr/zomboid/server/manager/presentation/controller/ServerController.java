package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ServerCommandService;
import com.apocalipsebr.zomboid.server.manager.application.service.ServerRestartService;
import com.apocalipsebr.zomboid.server.manager.application.service.ScheduledRestartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/server")
public class ServerController {
    private final ServerCommandService serverCommandService;
    private final ServerRestartService serverRestartService;
    private final ScheduledRestartService scheduledRestartService;

    public ServerController(ServerCommandService serverCommandService, 
                          ServerRestartService serverRestartService,
                          ScheduledRestartService scheduledRestartService) {
        this.serverCommandService = serverCommandService;
        this.serverRestartService = serverRestartService;
        this.scheduledRestartService = scheduledRestartService;
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
}
