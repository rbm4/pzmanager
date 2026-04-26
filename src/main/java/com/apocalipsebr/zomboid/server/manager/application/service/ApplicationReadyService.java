package com.apocalipsebr.zomboid.server.manager.application.service;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ApplicationReadyService {
    private static final Logger logger = Logger.getLogger(ApplicationReadyService.class.getName());

    @Value("${proxy.reconcile.enabled}")
    private boolean concileEnabled;

    private final RegionService regionService;
    private final SandboxPropertyService sandboxPropertyService;
    private final ScheduledRestartService scheduledRestartService;
    private final ServerBroadcastService serverBroadcastService;
    private final ProxyService proxyService;

    public ApplicationReadyService(
        RegionService regionService,
        SandboxPropertyService sandboxPropertyService,
        ScheduledRestartService scheduledRestartService,
        ServerBroadcastService serverBroadcastService,
        ProxyService proxyService
    ) {
        this.regionService = regionService;
        this.sandboxPropertyService = sandboxPropertyService;
        this.scheduledRestartService = scheduledRestartService;
        this.serverBroadcastService = serverBroadcastService;
        this.proxyService = proxyService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReadyEventTrigger() throws InterruptedException{
        Thread.sleep(5000);

        try {
            regionService.writeRegionsOnStartup();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to write regions on startup", e);
        } finally {
            logger.info("writeRegionsOnStartup completed");
        }
        Thread.sleep(2000);
        try {
            scheduledRestartService.startupMessage();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute startup message", e);
        } finally {
            logger.info("startupMessage completed");
        }
        Thread.sleep(2000);
        try {
            scheduledRestartService.executePendingSoftWipes();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute pending soft wipes", e);
        } finally {
            logger.info("executePendingSoftWipes completed");
        }
        Thread.sleep(2000);
        try {
            scheduledRestartService.scheduleNextRestart();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to schedule next restart", e);
        } finally {
            logger.info("scheduleNextRestart completed");
        }
        Thread.sleep(2000);
        try {
            serverBroadcastService.onApplicationReady();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to execute onApplicationReady broadcast", e);
        } finally {
            logger.info("onApplicationReady completed");
        }
        Thread.sleep(2000);
        try {
            sandboxPropertyService.syncSettingsOnStartup();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to sync sandbox settings on startup", e);
        } finally {
            logger.info("syncSettingsOnStartup completed");
        }
        Thread.sleep(2000);
        if (concileEnabled){
            try {
                proxyService.reconcile();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to reconcile proxy activations on startup", e);
            } finally {
                logger.info("proxyService.reconcile completed");
            }
        }
    }
}
