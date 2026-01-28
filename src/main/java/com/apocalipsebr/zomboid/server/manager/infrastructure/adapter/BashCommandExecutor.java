package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import com.apocalipsebr.zomboid.server.manager.domain.entity.ServerCommand;
import com.apocalipsebr.zomboid.server.manager.domain.exception.ServerCommandException;
import com.apocalipsebr.zomboid.server.manager.domain.port.ServerCommandExecutor;

import org.glavo.rcon.AuthenticationException;
import org.glavo.rcon.Rcon;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.logging.Logger;

@Component
public class BashCommandExecutor implements ServerCommandExecutor {
    private static final Logger logger = Logger.getLogger(BashCommandExecutor.class.getName());

    @Value("${zomboid.control.file:/opt/pzserver/zomboid.control}")
    private String controlFilePath;

    //

    @Override
    public String execute(ServerCommand command) {
        try {
            var rcon = new Rcon("72.62.137.60", 27015,"PzRconPaswd44@key");

            String result = rcon.command(command.getCommand());
            
            logger.info("Command executed successfully: " + command);
            logger.info(result);
            rcon.close();
            return result;
        } catch (IOException e) {
            throw new ServerCommandException("Failed to execute command: " + e.getMessage(), e);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            throw new ServerCommandException("Failed to execute command: " + e.getMessage(), e);
        }
    }
}
