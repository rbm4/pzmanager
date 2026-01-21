package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class ScheduledRestartService {
    private static final Logger logger = Logger.getLogger(ScheduledRestartService.class.getName());

    private final ServerRestartService serverRestartService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${server.scheduled.restart.enabled:true}")
    private boolean scheduledRestartEnabled;

    @Value("${server.scheduled.restart.hour:6}")
    private int restartHour;

    @Value("${server.scheduled.restart.minute:0}")
    private int restartMinute;

    @Value("${server.scheduled.restart.timezone:America/Sao_Paulo}")
    private String timezone;

    @Value("${server.restart.password}")
    private String restartPassword;

    public ScheduledRestartService(ServerRestartService serverRestartService) {
        this.serverRestartService = serverRestartService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void scheduleNextRestart() {
        if (!scheduledRestartEnabled) {
            logger.info("Scheduled automatic restart is disabled");
            return;
        }

        long delayInSeconds = calculateDelayUntilNextRestart();

        LocalDateTime nextRestartTime = LocalDateTime.now(ZoneId.of(timezone))
                .plusSeconds(delayInSeconds);

        logger.info(String.format(
                "Scheduling automatic server restart for: %s (in %d hours and %d minutes)",
                nextRestartTime,
                delayInSeconds / 3600,
                (delayInSeconds % 3600) / 60));

        scheduler.schedule(() -> {
            executeScheduledRestart();
        }, delayInSeconds, TimeUnit.SECONDS);
    }

    public long calculateDelayUntilNextRestart() {
        ZoneId zoneId = ZoneId.of(timezone);
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime nextHour = now.plusHours(4)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        return ChronoUnit.SECONDS.between(now, nextHour);
    }

    private void executeScheduledRestart() {
        try {
            logger.info("Executing scheduled automatic server restart");

            // Validate password internally since this is an automatic restart
            if (serverRestartService.validatePassword(restartPassword)) {
                serverRestartService.initiateRestart();

                // Schedule the next restart for tomorrow at the same time
                scheduleNextRestart();
            } else {
                logger.severe("Failed to validate restart password for scheduled restart");
            }

        } catch (Exception e) {
            logger.severe("Failed to execute scheduled restart: " + e.getMessage());
            e.printStackTrace();

            // Even if restart failed, schedule the next one
            scheduleNextRestart();
        }
    }

    public LocalDateTime getNextScheduledRestart() {
        ZoneId zoneId = ZoneId.of(timezone);
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime targetTime = now.toLocalDate()
                .atTime(LocalTime.of(restartHour, restartMinute));

        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }

        return targetTime;
    }

    public boolean isScheduledRestartEnabled() {
        return scheduledRestartEnabled;
    }
}
