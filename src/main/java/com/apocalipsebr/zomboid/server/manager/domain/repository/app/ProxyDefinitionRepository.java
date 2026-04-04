package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ProxyDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProxyDefinitionRepository extends JpaRepository<ProxyDefinition, Long> {

    Optional<ProxyDefinition> findByProxyId(String proxyId);

    List<ProxyDefinition> findByEnabledTrue();

    List<ProxyDefinition> findByProviderType(String providerType);
}
