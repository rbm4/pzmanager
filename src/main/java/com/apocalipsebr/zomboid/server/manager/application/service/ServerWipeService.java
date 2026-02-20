package com.apocalipsebr.zomboid.server.manager.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ServerWipeService {

    private static final Logger log = LoggerFactory.getLogger(ServerWipeService.class);
    private static final String WIPE_PASSWORD = "wipe-apocalipse-2025!";
    private static final String MULTIPLAYER_FOLDER = "Multiplayer";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Value("${server.save.path:}")
    private String serverSavePath;

    public boolean validatePassword(String password) {
        return WIPE_PASSWORD.equals(password);
    }

    public WipeResult wipeServer() {
        if (serverSavePath == null || serverSavePath.isBlank()) {
            return WipeResult.failure("Server save path is not configured (server.save.path).");
        }

        Path savePath = Paths.get(serverSavePath);
        if (!Files.exists(savePath) || !Files.isDirectory(savePath)) {
            return WipeResult.failure("Save path does not exist or is not a directory: " + serverSavePath);
        }

        Path multiplayerPath = savePath.resolve(MULTIPLAYER_FOLDER);
        if (!Files.exists(multiplayerPath)) {
            return WipeResult.failure("Multiplayer folder not found at: " + multiplayerPath);
        }

        if (!Files.isDirectory(multiplayerPath)) {
            return WipeResult.failure("Multiplayer path exists but is not a directory: " + multiplayerPath);
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = MULTIPLAYER_FOLDER + "_backup_" + timestamp;
        Path backupPath = savePath.resolve(backupName);

        try {
            Files.move(multiplayerPath, backupPath);
            log.info("Server wipe completed. Renamed '{}' to '{}'", multiplayerPath, backupPath);
            return WipeResult.success(backupName, multiplayerPath.toString(), backupPath.toString());
        } catch (IOException e) {
            log.error("Failed to rename Multiplayer folder: {}", e.getMessage(), e);
            return WipeResult.failure("Failed to rename Multiplayer folder: " + e.getMessage()
                    + ". Make sure the server is completely shut down and no files are locked.");
        }
    }

    public static class WipeResult {
        private final boolean success;
        private final String message;
        private final String backupName;
        private final String originalPath;
        private final String backupPath;

        private WipeResult(boolean success, String message, String backupName, String originalPath, String backupPath) {
            this.success = success;
            this.message = message;
            this.backupName = backupName;
            this.originalPath = originalPath;
            this.backupPath = backupPath;
        }

        public static WipeResult success(String backupName, String originalPath, String backupPath) {
            return new WipeResult(true,
                    "Server wipe completed successfully! Folder renamed to: " + backupName,
                    backupName, originalPath, backupPath);
        }

        public static WipeResult failure(String message) {
            return new WipeResult(false, message, null, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getBackupName() { return backupName; }
        public String getOriginalPath() { return originalPath; }
        public String getBackupPath() { return backupPath; }
    }
}
