package com.apocalipsebr.zomboid.server.manager.application.constants;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Component
public class RadioReset {
    
    private static final Logger logger = LoggerFactory.getLogger(RadioReset.class);
    
    @Value("${radio.save.file.path}")
    private String radioSaveFilePath;
    
    @PostConstruct
    public void resetRadioData() {
        Path radioSavePath = Paths.get(radioSaveFilePath);
        
        if (!Files.exists(radioSavePath)) {
            logger.info("RADIO_SAVE.txt not found at: {}. Skipping radio reset.", radioSaveFilePath);
            return;
        }
        
        try {
            List<String> lines = Files.readAllLines(radioSavePath);
            List<String> updatedLines = new ArrayList<>();
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                
                if (i == 0 && line.startsWith("DaysSinceStart")) {
                    // Update DaysSinceStart to 0
                    updatedLines.add("DaysSinceStart = 0");
                } else if (i > 1) {
                    // Update channel lines (after first 2 lines)
                    updatedLines.add(updateChannelLine(line));
                } else {
                    // Keep other lines as is
                    updatedLines.add(line);
                }
            }
            
            Files.write(radioSavePath, updatedLines, StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("Successfully reset RADIO_SAVE.txt at: {}", radioSaveFilePath);
            
        } catch (IOException e) {
            logger.error("Error processing RADIO_SAVE.txt: {}", e.getMessage(), e);
        }
    }
    
    private String updateChannelLine(String line) {
        String[] parts = line.split(",");
        
        if (parts.length >= 5) {
            // Update [Current Script Name] to "main" if it's "none" (index 3)
            if ("none".equals(parts[3])) {
                parts[3] = "main";
            }
            
            // Update [Script Start Day] to 0 (index 4)
            parts[4] = "0";
        }
        
        return String.join(",", parts);
    }
}
