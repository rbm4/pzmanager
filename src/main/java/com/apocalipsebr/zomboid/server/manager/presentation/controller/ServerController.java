package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ServerCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/server")
public class ServerController {
    private final ServerCommandService serverCommandService;

    public ServerController(ServerCommandService serverCommandService) {
        this.serverCommandService = serverCommandService;
    }

    @PostMapping("/command")
    public ResponseEntity<String> sendCommand(@RequestBody CommandRequest request) {
        serverCommandService.sendCommand(request.getCommand());
        return ResponseEntity.ok("Command sent successfully: " + request.getCommand());
    }

    @PostMapping("/command/text")
    public ResponseEntity<String> sendCommandAsText(@RequestParam String command) {
        serverCommandService.sendCommand(command);
        return ResponseEntity.ok("Command sent successfully: " + command);
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
