package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

public interface ProxyProvider {

    String getProviderType();

    void startInstance(String instanceId, String region);

    void stopInstance(String instanceId, String region);

    ProxyInstanceState getInstanceState(String instanceId, String region);

    boolean isConfigured();
}
