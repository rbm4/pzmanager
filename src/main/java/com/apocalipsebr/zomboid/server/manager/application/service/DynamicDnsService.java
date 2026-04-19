package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.infrastructure.adapter.HostingerApiClient;
import com.apocalipsebr.zomboid.server.manager.infrastructure.config.HostingerConfig.HostingerProperties;
import jakarta.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class DynamicDnsService {

    private static final Logger logger = Logger.getLogger(DynamicDnsService.class.getName());
    private static final String IP_CHECK_URL = "https://api.ipify.org";

    private final HostingerApiClient hostingerApiClient;
    private final HostingerProperties hostingerProperties;
    private final OkHttpClient httpClient;

    @Value("${ddns.enabled:false}")
    private boolean enabled;

    @Value("${ddns.subdomain:develop}")
    private String subdomain;

    @Value("${ddns.cleanup-on-shutdown:true}")
    private boolean cleanupOnShutdown;

    private String lastKnownIp;
    private LocalDateTime lastUpdatedAt;
    private String lastError;

    public DynamicDnsService(HostingerApiClient hostingerApiClient, HostingerProperties hostingerProperties) {
        this.hostingerApiClient = hostingerApiClient;
        this.hostingerProperties = hostingerProperties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Scheduled(fixedRateString = "${ddns.check-interval-ms:300000}")
    public void checkAndUpdateDns() {
        if (!enabled) {
            return;
        }
        if (!hostingerApiClient.isConfigured()) {
            logger.warning("DDNS enabled but Hostinger API key not configured");
            return;
        }

        try {
            String currentIp = fetchPublicIp();
            if (currentIp == null || currentIp.isBlank()) {
                lastError = "Failed to resolve public IP";
                logger.warning(lastError);
                return;
            }

            if (currentIp.equals(lastKnownIp)) {
                logger.fine("DDNS: IP unchanged (" + currentIp + "), skipping update");
                return;
            }

            String previousIp = lastKnownIp;
            hostingerApiClient.updateDnsRecord(
                    hostingerProperties.getBaseDomain(),
                    subdomain,
                    currentIp,
                    hostingerProperties.getDnsTtl());

            lastKnownIp = currentIp;
            lastUpdatedAt = LocalDateTime.now();
            lastError = null;

            String fqdn = subdomain + "." + hostingerProperties.getBaseDomain();
            logger.info("DDNS updated: " + fqdn + " → " + currentIp
                    + (previousIp != null ? " (was " + previousIp + ")" : " (initial)"));

        } catch (Exception e) {
            lastError = e.getMessage();
            logger.log(Level.WARNING, "DDNS update failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (!enabled || !cleanupOnShutdown || lastKnownIp == null) {
            return;
        }
        try {
            hostingerApiClient.deleteDnsRecord(hostingerProperties.getBaseDomain(), subdomain);
            String fqdn = subdomain + "." + hostingerProperties.getBaseDomain();
            logger.info("DDNS cleanup: removed " + fqdn + " on shutdown");
        } catch (Exception e) {
            logger.log(Level.WARNING, "DDNS cleanup failed on shutdown", e);
        }
    }

    public void forceUpdate() {
        lastKnownIp = null;
        checkAndUpdateDns();
    }

    private String fetchPublicIp() throws IOException {
        Request request = new Request.Builder()
                .url(IP_CHECK_URL)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("IP check failed: HTTP " + response.code());
            }
            return response.body().string().trim();
        }
    }

    // --- Status getters for admin endpoint ---

    public boolean isEnabled() { return enabled; }

    public String getSubdomain() { return subdomain; }

    public String getFqdn() {
        return subdomain + "." + hostingerProperties.getBaseDomain();
    }

    public String getLastKnownIp() { return lastKnownIp; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }

    public String getLastError() { return lastError; }
}
