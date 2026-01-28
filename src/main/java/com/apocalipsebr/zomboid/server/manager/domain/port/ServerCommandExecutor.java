package com.apocalipsebr.zomboid.server.manager.domain.port;

import com.apocalipsebr.zomboid.server.manager.domain.entity.ServerCommand;

public interface ServerCommandExecutor {
    String execute(ServerCommand command);
}
