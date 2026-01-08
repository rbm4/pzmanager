package com.apocalipsebr.zomboid.server.manager.domain.exception;

public class ServerCommandException extends RuntimeException {
    public ServerCommandException(String message) {
        super(message);
    }

    public ServerCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
