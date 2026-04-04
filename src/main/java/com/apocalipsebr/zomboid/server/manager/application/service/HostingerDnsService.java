package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.infrastructure.adapter.HostingerApiClient;
import com.apocalipsebr.zomboid.server.manager.infrastructure.config.HostingerConfig.HostingerProperties;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class HostingerDnsService {

    private static final Logger logger = Logger.getLogger(HostingerDnsService.class.getName());

    private final HostingerApiClient hostingerApiClient;
    private final HostingerProperties hostingerProperties;

    public HostingerDnsService(HostingerApiClient hostingerApiClient, HostingerProperties hostingerProperties) {
        this.hostingerApiClient = hostingerApiClient;
        this.hostingerProperties = hostingerProperties;
    }

    public void updateProxyDns(String dnsSubdomain, String ip) {
        if (!hostingerApiClient.isConfigured()) {
            logger.warning("Hostinger API not configured — skipping DNS update for " + dnsSubdomain);
            return;
        }
        try {
            hostingerApiClient.updateDnsRecord(
                    hostingerProperties.getBaseDomain(),
                    dnsSubdomain,
                    ip,
                    hostingerProperties.getDnsTtl());
            logger.info("DNS updated: " + dnsSubdomain + "." + hostingerProperties.getBaseDomain() + " → " + ip);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update DNS for " + dnsSubdomain + " → " + ip, e);
        }
    }

    public void removeProxyDns(String dnsSubdomain) {
        if (!hostingerApiClient.isConfigured()) {
            logger.warning("Hostinger API not configured — skipping DNS removal for " + dnsSubdomain);
            return;
        }
        try {
            hostingerApiClient.deleteDnsRecord(
                    hostingerProperties.getBaseDomain(),
                    dnsSubdomain);
            logger.info("DNS removed: " + dnsSubdomain + "." + hostingerProperties.getBaseDomain());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to remove DNS for " + dnsSubdomain, e);
        }
    }

    public String buildDnsName(String dnsSubdomain) {
        return dnsSubdomain + "." + hostingerProperties.getBaseDomain();
    }
}
