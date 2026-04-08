package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Component
public class ServerBroadcastService {
    private static final Logger logger = Logger.getLogger(ServerBroadcastService.class.getName());

    private final ServerCommandService commandService;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicInteger messageIndex = new AtomicInteger(0);

    @Value("${server.boot.sequence.enabled:false}")
    private boolean enabled;

    @Value("${server.scheduled.restart.hour:6}")
    private int restartCycleHours;

    private static final int MESSAGES_PER_CYCLE = 2;
    private static final int RESERVED_MINUTES = 11;

    private static final List<String> BROADCAST_MESSAGES = List.of(
            "Bem-vindo ao Apocalipse BR! Divirta-se e respeite as regras.",
            "Iniciem as pesquisas! A vacina sob patente da Umbrella corp já está em Knox County! Https://apocalipse.cloud/season-guide",
            "Cada zumbi abatido você ganha saldo para comprar itens e carros no site! Https://apocalipse.cloud/",
            "Problemas de conexão? Teste um de nossos proxies! Https://apocalipse.cloud/proxy",
            "Lembre-se: trabalho em equipe aumenta suas chances de sobrevivência!",
            "Abates de Zumbis com efeitos de zona dão saldo extra no site!",
            "Doe para manter o servidor online! Acesse a aba de doações no site.",
            "Novidades e atualizações sao anunciadas no Discord. Fique ligado!",
            "Áreas PVP são caracterizadas pela caveira ao lado do seu nome. Cuidado!"
    );

    public ServerBroadcastService(ServerCommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * Calculates the interval in milliseconds between each broadcast message.
     * The total cycle time is (restartCycleHours * 60 - RESERVED_MINUTES) minutes,
     * divided by (number of messages * MESSAGES_PER_CYCLE) to get the interval.
     */
    private long calculateIntervalMs() {
        int totalMinutes = restartCycleHours * 60 - RESERVED_MINUTES;
        int totalMessages = BROADCAST_MESSAGES.size() * MESSAGES_PER_CYCLE;
        long intervalMs = (long) totalMinutes * 60 * 1000 / totalMessages;
        return intervalMs;
    }

    public void onApplicationReady() {
        if (!enabled) {
            logger.info("ServerBroadcastService disabled by configuration.");
            return;
        }

        long intervalMs = calculateIntervalMs();
        logger.info(String.format(
                "ServerBroadcastService initialized - restart cycle: %dh, interval: %.2f min, %d messages x%d cycles.",
                restartCycleHours, intervalMs / 60000.0, BROADCAST_MESSAGES.size(), MESSAGES_PER_CYCLE));

        scheduler.schedule(this::broadcastMessage, intervalMs, TimeUnit.MILLISECONDS);
    }

    public void broadcastMessage() {
        int index = messageIndex.getAndUpdate(i -> (i + 1) % BROADCAST_MESSAGES.size());
        String message = BROADCAST_MESSAGES.get(index);
        try {
            String command = "servermsg \"" + message + "\"";
            logger.info("Broadcasting message: " + command);
            commandService.sendCommand(command);
        } catch (Exception e) {
            logger.warning("Failed to broadcast message: " + e.getMessage());
        }

        // Schedule the next broadcast
        long intervalMs = calculateIntervalMs();
        scheduler.schedule(this::broadcastMessage, intervalMs, TimeUnit.MILLISECONDS);
    }
}
