package com.apocalipsebr.zomboid.server.manager.domain.port;

import com.apocalipsebr.zomboid.server.manager.domain.entity.ServerCommand;

public interface ServerCommandExecutor {
    void execute(ServerCommand command);

    String executeResponse(ServerCommand command);
}
