package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proxy_definition")
public class ProxyDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proxy_id", nullable = false, unique = true, length = 50)
    private String proxyId;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "provider_type", nullable = false, length = 30)
    private String providerType;

    @Column(name = "instance_id", nullable = false, length = 100)
    private String instanceId;

    @Column(name = "provider_region", length = 30)
    private String providerRegion;

    @Column(name = "dns_subdomain", nullable = false, length = 100)
    private String dnsSubdomain;

    @Column(name = "port", nullable = false)
    private Integer port = 16261;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ProxyDefinition() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProxyId() { return proxyId; }
    public void setProxyId(String proxyId) { this.proxyId = proxyId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProviderType() { return providerType; }
    public void setProviderType(String providerType) { this.providerType = providerType; }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getProviderRegion() { return providerRegion; }
    public void setProviderRegion(String providerRegion) { this.providerRegion = providerRegion; }

    public String getDnsSubdomain() { return dnsSubdomain; }
    public void setDnsSubdomain(String dnsSubdomain) { this.dnsSubdomain = dnsSubdomain; }

    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
