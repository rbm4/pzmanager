package com.apocalipsebr.zomboid.server.manager.domain.entity;

public class ServerCommand {
    private final String command;
    private final long timestamp;

    public ServerCommand(String command) {
        this.command = command;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCommand() {
        return command;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ServerCommand{" +
                "command='" + command + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
