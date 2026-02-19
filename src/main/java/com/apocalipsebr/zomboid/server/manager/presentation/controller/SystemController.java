package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    private static final Logger logger = Logger.getLogger(SystemController.class.getName());
    
    @Value("${server.restart.password}")
    private String restartPassword;
    
    @Value("${app.version:unknown}")
    private String appVersion;
    
    /**
     * Restart the application
     * POST /api/system/restart
     * Body: { "password": "your-password" }
     */
    @PostMapping("/restart")
    public ResponseEntity<Map<String, Object>> restart(@RequestBody Map<String, String> payload) {
        
        Map<String, Object> response = new HashMap<>();
        String password = payload.get("password");
        
        // Validate password
        if (password == null || !restartPassword.equals(password)) {
            logger.warning("Unauthorized restart attempt");
            response.put("success", false);
            response.put("message", "Invalid password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        logger.info("Restart triggered via API. Shutting down in 5 seconds...");
        
        response.put("success", true);
        response.put("message", "Application restarting. Git pull will be executed on startup.");
        response.put("timestamp", System.currentTimeMillis());
        
        // Schedule restart in separate thread to allow response to be sent
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait 5 seconds
                logger.info("Executing shutdown...");
                System.exit(0); // SystemD will restart the service
            } catch (InterruptedException e) {
                logger.severe("Restart interrupted: " + e.getMessage());
            }
        }).start();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * GET /api/system/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("version", appVersion);
        response.put("timestamp", System.currentTimeMillis());
        response.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        return ResponseEntity.ok(response);
    }
}
