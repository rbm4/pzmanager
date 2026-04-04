package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

public record ProxyInstanceState(String state, String publicIp) {

    public boolean isRunning() {
        return "running".equals(state);
    }

    public boolean isStopped() {
        return "stopped".equals(state);
    }

    public boolean isTerminated() {
        return "terminated".equals(state);
    }

    public boolean hasPublicIp() {
        return publicIp != null && !publicIp.isBlank();
    }
}
