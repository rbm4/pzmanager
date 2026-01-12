package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final FileService fileService;

    public LogController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> listLogs() {
        try {
            List<String> files = fileService.listLogFiles();
            return ResponseEntity.ok(Map.of(
                "files", files,
                "count", files.size(),
                "path", fileService.getLogsPath()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", e.getMessage(),
                    "path", fileService.getLogsPath()
                ));
        }
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<String> viewLog(
            @PathVariable String filename,
            @RequestParam(defaultValue = "100") int lines) {
        try {
            String content = fileService.getLogFileContent(filename, lines);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Error reading file: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadLog(@PathVariable String filename) {
        try {
            Resource file = fileService.getLogFile(filename);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/tail/{filename}")
    public ResponseEntity<String> tailLog(
            @PathVariable String filename,
            @RequestParam(defaultValue = "50") int lines) {
        try {
            String content = fileService.getLogFileContent(filename, lines);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header("X-Log-Lines", String.valueOf(lines))
                .body(content);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Access denied");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("File not found: " + filename);
        }
    }
}
