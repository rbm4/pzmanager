package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.apocalipsebr.zomboid.server.manager.presentation.controller.ZombieKillsController;

public class ApplicationReadyService {
    private final RegionService regionService;
    private final SandboxPropertyService sandboxPropertyService;
    private final ScheduledRestartService scheduledRestartService;
    private final ServerBroadcastService serverBroadcastService;
    private final ZombieKillsController zombieKillsController;

    public ApplicationReadyService(
        RegionService regionService,
        SandboxPropertyService sandboxPropertyService,
        ScheduledRestartService scheduledRestartService,
        ServerBroadcastService serverBroadcastService,
        ZombieKillsController zombieKillsController
    ) {
        this.regionService = regionService;
        this.sandboxPropertyService = sandboxPropertyService;
        this.scheduledRestartService = scheduledRestartService;
        this.serverBroadcastService = serverBroadcastService;
        this.zombieKillsController = zombieKillsController;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReadyEventTrigger() throws InterruptedException{
        Thread.sleep(5000);
        regionService.writeRegionsOnStartup();
        sandboxPropertyService.syncSettingsOnStartup();
        scheduledRestartService.startupMessage();
        scheduledRestartService.executePendingSoftWipes();
        scheduledRestartService.scheduleNextRestart();
        serverBroadcastService.onApplicationReady();
    }
}
