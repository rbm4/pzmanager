package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import com.apocalipsebr.zomboid.server.manager.domain.entity.ServerCommand;
import com.apocalipsebr.zomboid.server.manager.domain.exception.ServerCommandException;
import com.apocalipsebr.zomboid.server.manager.domain.port.ServerCommandExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class BashCommandExecutor implements ServerCommandExecutor {
    private static final Logger logger = Logger.getLogger(BashCommandExecutor.class.getName());

    @Value("${zomboid.control.file:/opt/pzserver/zomboid.control}")
    private String controlFilePath;

    @Override
    public void execute(ServerCommand command) {
        try {
            String bashCommand = String.format("echo \"%s\" > %s", command.getCommand(), controlFilePath);
            
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", bashCommand);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode != 0) {
                throw new ServerCommandException("Command execution failed with exit code: " + exitCode);
            }
            
            logger.info("Command executed successfully: " + command);
        } catch (IOException e) {
            throw new ServerCommandException("Failed to execute command: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerCommandException("Command execution was interrupted: " + e.getMessage(), e);
        }
    }
}
