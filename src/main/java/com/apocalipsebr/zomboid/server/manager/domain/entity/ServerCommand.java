package com.apocalipsebr.zomboid.server.manager.domain.entity;

public class ServerCommand {
    private final String command;
    private final long timestamp;
    private final int responseWaitTime;

    public ServerCommand(String command) {
        this.command = command;
        this.timestamp = System.currentTimeMillis();
        this.responseWaitTime = 1000;
    }

    public String getCommand() {
        return command;
    }

    public int getResponseWaitTime(){
        return responseWaitTime;
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
