package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ProxyService;
import com.apocalipsebr.zomboid.server.manager.application.service.ProxyService.ActivateResult;
import com.apocalipsebr.zomboid.server.manager.application.service.ProxyService.ExtendResult;
import com.apocalipsebr.zomboid.server.manager.application.service.ProxyService.ProxyInfo;
import com.apocalipsebr.zomboid.server.manager.application.service.ProxyService.StatusResult;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ProxyActivation;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ProxyDefinition;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ProxyDefinitionRepository;
import com.apocalipsebr.zomboid.server.manager.infrastructure.config.AwsProxyConfig.ProxyProperties;
import com.apocalipsebr.zomboid.server.manager.infrastructure.config.HostingerConfig.HostingerProperties;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.ProxyActivateRequest;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.ProxyExtendRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    private static final Logger logger = Logger.getLogger(ProxyController.class.getName());

    private final ProxyService proxyService;
    private final ProxyProperties proxyProperties;
    private final ProxyDefinitionRepository proxyDefinitionRepository;
    private final HostingerProperties hostingerProperties;

    public ProxyController(ProxyService proxyService, ProxyProperties proxyProperties,
                           ProxyDefinitionRepository proxyDefinitionRepository,
                           HostingerProperties hostingerProperties) {
        this.proxyService = proxyService;
        this.proxyProperties = proxyProperties;
        this.proxyDefinitionRepository = proxyDefinitionRepository;
        this.hostingerProperties = hostingerProperties;
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        StatusResult status = proxyService.getStatus(user);

        List<Map<String, Object>> proxies = status.proxies().stream().map(p -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("proxyId", p.proxyId());
            map.put("region", p.region());
            map.put("port", p.port());
            map.put("status", p.status());
            if ("ACTIVE".equals(p.status()) || "STARTING".equals(p.status())) {
                map.put("address", p.address());
                map.put("activatedBy", p.activatedBy());
                map.put("expiresAt", p.expiresAt());
                map.put("remainingMinutes", p.remainingMinutes());
                map.put("activationId", p.activationId());
                map.put("hours", p.hours());
            }
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "proxies", proxies,
                "userCredits", status.userCredits(),
                "pricing", Map.of(
                        "creditsPer24h", proxyProperties.getCreditsPer24h(),
                        "minHours", proxyProperties.getMinHours(),
                        "maxHours", proxyProperties.getMaxHours(),
                        "hourStep", proxyProperties.getHourStep()
                )
        ));
    }

    @PostMapping("/activate")
    public ResponseEntity<?> activate(@RequestBody ProxyActivateRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        ActivateResult result = proxyService.activate(user, request.proxyId(), request.hours());

        if (!result.success()) {
            HttpStatus status = switch (result.errorCode()) {
                case 402 -> HttpStatus.PAYMENT_REQUIRED;
                case 409 -> HttpStatus.CONFLICT;
                default -> HttpStatus.BAD_REQUEST;
            };

            Map<String, Object> error = new LinkedHashMap<>();
            error.put("error", result.error());
            if (result.errorCode() == 402) {
                error.put("creditsRequired", proxyProperties.calculateCredits(request.hours()));
                error.put("creditsAvailable", proxyService.getStatus(user).userCredits());
            }
            return ResponseEntity.status(status).body(error);
        }

        ProxyActivation activation = result.activation();
        String address = proxyDefinitionRepository.findByProxyId(activation.getProxyId())
                .map(def -> def.getDnsSubdomain() + "." + hostingerProperties.getBaseDomain())
                .orElse("");
        int port = proxyDefinitionRepository.findByProxyId(activation.getProxyId())
                .map(ProxyDefinition::getPort)
                .orElse(16261);

        return ResponseEntity.ok(Map.of(
                "activationId", activation.getId(),
                "proxyId", activation.getProxyId(),
                "address", address,
                "port", port,
                "hours", activation.getHours(),
                "creditsSpent", activation.getCreditsSpent(),
                "expiresAt", activation.getExpiresAt(),
                "status", activation.getStatus()
        ));
    }

    @PostMapping("/extend")
    public ResponseEntity<?> extend(@RequestBody ProxyExtendRequest request) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        ExtendResult result = proxyService.extend(user, request.activationId(), request.additionalHours());

        if (!result.success()) {
            HttpStatus status = switch (result.errorCode()) {
                case 402 -> HttpStatus.PAYMENT_REQUIRED;
                case 403 -> HttpStatus.FORBIDDEN;
                case 404 -> HttpStatus.NOT_FOUND;
                default -> HttpStatus.BAD_REQUEST;
            };
            return ResponseEntity.status(status).body(Map.of("error", result.error()));
        }

        return ResponseEntity.ok(Map.of(
                "activationId", request.activationId(),
                "newExpiresAt", result.newExpiresAt(),
                "additionalCreditsSpent", result.additionalCreditsSpent(),
                "totalHours", result.totalHours()
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProxyActivation> activations = proxyService.getHistory(user, pageable);

        List<Map<String, Object>> items = activations.getContent().stream().map(a -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", a.getId());
            map.put("proxyId", a.getProxyId());
            map.put("hours", a.getHours());
            map.put("creditsSpent", a.getCreditsSpent());
            map.put("activatedAt", a.getActivatedAt());
            map.put("expiresAt", a.getExpiresAt());
            map.put("stoppedAt", a.getStoppedAt());
            map.put("status", a.getStatus());
            return map;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "activations", items,
                "page", activations.getNumber(),
                "totalPages", activations.getTotalPages()
        ));
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            return (User) authentication.getPrincipal();
        } catch (Exception e) {
            logger.warning("Error getting current user: " + e.getMessage());
            return null;
        }
    }
}
