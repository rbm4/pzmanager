package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ProxyExpirationPoller {

    private static final Logger logger = Logger.getLogger(ProxyExpirationPoller.class.getName());

    private final ProxyService proxyService;

    public ProxyExpirationPoller(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @Scheduled(fixedRateString = "${aws.proxy.poll-interval-seconds:60}000")
    public void checkExpiredActivations() {
        try {
            proxyService.processExpiredActivations();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during proxy expiration poll", e);
        }
    }
}
