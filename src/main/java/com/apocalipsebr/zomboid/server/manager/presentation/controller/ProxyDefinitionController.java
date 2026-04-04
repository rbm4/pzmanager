package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ProxyDefinition;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ProxyDefinitionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/admin/proxies")
@PreAuthorize("hasRole('ADMIN')")
public class ProxyDefinitionController {

    private static final Logger logger = Logger.getLogger(ProxyDefinitionController.class.getName());

    private final ProxyDefinitionRepository proxyDefinitionRepository;

    public ProxyDefinitionController(ProxyDefinitionRepository proxyDefinitionRepository) {
        this.proxyDefinitionRepository = proxyDefinitionRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProxyDefinition>> listAll() {
        return ResponseEntity.ok(proxyDefinitionRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return proxyDefinitionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProxyDefinition definition) {
        if (definition.getProxyId() == null || definition.getProxyId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "proxyId is required"));
        }
        if (definition.getDisplayName() == null || definition.getDisplayName().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "displayName is required"));
        }
        if (definition.getInstanceId() == null || definition.getInstanceId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "instanceId is required"));
        }
        if (definition.getDnsSubdomain() == null || definition.getDnsSubdomain().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "dnsSubdomain is required"));
        }
        if (proxyDefinitionRepository.findByProxyId(definition.getProxyId()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A proxy with this proxyId already exists"));
        }

        if (definition.getProviderType() == null || definition.getProviderType().isBlank()) {
            definition.setProviderType("AWS_EC2");
        }
        if (definition.getProviderRegion() == null || definition.getProviderRegion().isBlank()) {
            definition.setProviderRegion("sa-east-1");
        }
        if (definition.getPort() == null) {
            definition.setPort(16261);
        }
        if (definition.getEnabled() == null) {
            definition.setEnabled(true);
        }

        ProxyDefinition saved = proxyDefinitionRepository.save(definition);
        logger.info("Created proxy definition: " + saved.getProxyId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProxyDefinition update) {
        return proxyDefinitionRepository.findById(id).map(existing -> {
            if (update.getDisplayName() != null && !update.getDisplayName().isBlank()) {
                existing.setDisplayName(update.getDisplayName());
            }
            if (update.getProviderType() != null && !update.getProviderType().isBlank()) {
                existing.setProviderType(update.getProviderType());
            }
            if (update.getInstanceId() != null && !update.getInstanceId().isBlank()) {
                existing.setInstanceId(update.getInstanceId());
            }
            if (update.getProviderRegion() != null && !update.getProviderRegion().isBlank()) {
                existing.setProviderRegion(update.getProviderRegion());
            }
            if (update.getDnsSubdomain() != null && !update.getDnsSubdomain().isBlank()) {
                existing.setDnsSubdomain(update.getDnsSubdomain());
            }
            if (update.getPort() != null) {
                existing.setPort(update.getPort());
            }
            if (update.getEnabled() != null) {
                existing.setEnabled(update.getEnabled());
            }

            ProxyDefinition saved = proxyDefinitionRepository.save(existing);
            logger.info("Updated proxy definition: " + saved.getProxyId());
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return proxyDefinitionRepository.findById(id).map(existing -> {
            proxyDefinitionRepository.delete(existing);
            logger.info("Deleted proxy definition: " + existing.getProxyId());
            return ResponseEntity.ok(Map.of("deleted", existing.getProxyId()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
