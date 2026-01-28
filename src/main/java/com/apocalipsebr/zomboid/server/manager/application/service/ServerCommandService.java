package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.ServerCommand;
import com.apocalipsebr.zomboid.server.manager.domain.port.ServerCommandExecutor;
import org.springframework.stereotype.Service;

@Service
public class ServerCommandService {
    private final ServerCommandExecutor commandExecutor;

    public ServerCommandService(ServerCommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public String sendCommand(String command) {
        ServerCommand serverCommand = new ServerCommand(command);
        return commandExecutor.execute(serverCommand);
    }
}
