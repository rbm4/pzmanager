package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Component
public class ServerBroadcastService {
    private static final Logger logger = Logger.getLogger(ServerBroadcastService.class.getName());

    private final ServerCommandService commandService;
    private final AtomicInteger messageIndex = new AtomicInteger(0);
    private boolean applicationReady = false;

    @Value("${server.boot.sequence.enabled:false}")
    private boolean enabled;

    private static final List<String> BROADCAST_MESSAGES = List.of(
            "Bem-vindo ao Apocalipse BR! Divirta-se e respeite as regras.",
            "Visite nosso site para conferir a loja, garagem e mais! Https://apocalipse.cloud/",
            "Participe dos eventos semanais e ganhe recompensas!",
            "Lembre-se: trabalho em equipe aumenta suas chances de sobrevivência!",
            "Super-zumbis em Westpoint, Ekron, Bradeburg, Riverside dão 3x saldo extra no site!",
            "Doe para manter o servidor online! Acesse a aba de doações no site.",
            "Novidades e atualizações sao anunciadas no Discord. Fique ligado!",
            "Cada zumbi abatido você ganha saldo para comprar itens e carros no site! Https://apocalipse.cloud/",
            "Áreas PVP são caracterizadas pela caveira ao lado do seu nome. Cuidado!",
            "Áreas militares, grandes rodovias, estações de polícia e lojas de armas são áreas PVP!"
    );

    public ServerBroadcastService(ServerCommandService commandService) {
        this.commandService = commandService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!enabled) {
            logger.info("ServerBroadcastService disabled by configuration.");
            return;
        }
        applicationReady = true;
        logger.info("ServerBroadcastService initialized - scheduled broadcasts enabled.");
    }

    @Scheduled(fixedRate = 3600000)
    public void broadcastMessage() {
        if (!applicationReady) {
            return;
        }

        int index = messageIndex.getAndUpdate(i -> (i + 1) % BROADCAST_MESSAGES.size());
        String message = BROADCAST_MESSAGES.get(index);

        try {
            String command = "servermsg \"" + message + "\"";
            logger.info("Broadcasting message: " + command);
            commandService.sendCommand(command);
        } catch (Exception e) {
            logger.warning("Failed to broadcast message: " + e.getMessage());
        }
    }
}
