package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {
    private static final Logger logger = Logger.getLogger(FileService.class.getName());

    @Value("${zomboid.logs.path:/home/pzuser/Zomboid/Logs}")
    private String logsPath;

    @Value("${zomboid.server.path:/opt/pzserver}")
    private String serverPath;

    public Resource getLogFile(String filename) throws IOException {
        Path filePath = Paths.get(logsPath, filename);
        
        // Security check - prevent directory traversal
        if (!filePath.normalize().startsWith(Paths.get(logsPath).normalize())) {
            throw new SecurityException("Access denied");
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found: " + filename);
        }

        logger.info("Serving file: " + filePath);
        return new FileSystemResource(file);
    }

    public String getLogFileContent(String filename, int lines) throws IOException {
        Path filePath = Paths.get(logsPath, filename);
        
        // Security check
        if (!filePath.normalize().startsWith(Paths.get(logsPath).normalize())) {
            throw new SecurityException("Access denied");
        }

        File file = filePath.toFile();
        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found: " + filename);
        }

        // Read last N lines
        List<String> allLines = Files.readAllLines(filePath);
        if (allLines.isEmpty()) {
            return "";
        }
        
        int startIndex = Math.max(0, allLines.size() - lines);
        return String.join("\n", allLines.subList(startIndex, allLines.size()));
    }

    public List<String> listLogFiles() throws IOException {
        Path logsDir = Paths.get(logsPath);
        
        if (!Files.exists(logsDir)) {
            throw new IOException("Logs directory not found: " + logsPath);
        }

        try (Stream<Path> paths = Files.walk(logsDir, 2)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".txt") || name.endsWith(".log");
                })
                .map(p -> logsDir.relativize(p).toString())
                .sorted()
                .collect(Collectors.toList());
        }
    }

    public String getLogsPath() {
        return logsPath;
    }
}
