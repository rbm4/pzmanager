package com.apocalipsebr.zomboid.server.manager.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class DiscordNotificationService {
    private static final Logger logger = Logger.getLogger(DiscordNotificationService.class.getName());

    private final RestTemplate restTemplate;

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    @Value("${discord.notifications.enabled:false}")
    private boolean notificationsEnabled;

    public DiscordNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendRestartWarning(String timeRemaining) {
        if (!notificationsEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            logger.fine("Discord notifications are disabled or webhook URL is not configured");
            return;
        }

        try {
            Map<String, Object> payload = buildWarningMessage(timeRemaining);
            sendWebhook(payload);
            logger.info("Discord warning sent successfully for: " + timeRemaining);
        } catch (Exception e) {
            logger.warning("Failed to send Discord notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendRestartInitiated() {
        if (!notificationsEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = buildInitiatedMessage();
            sendWebhook(payload);
            logger.info("Discord restart initiated notification sent");
        } catch (Exception e) {
            logger.warning("Failed to send Discord notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendServerShuttingDown() {
        if (!notificationsEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = buildShutdownMessage();
            sendWebhook(payload);
            logger.info("Discord shutdown notification sent");
        } catch (Exception e) {
            logger.warning("Failed to send Discord notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Object> buildWarningMessage(String timeRemaining) {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        
        return Map.of(
            "embeds", List.of(
                Map.of(
                    "title", "⚠️ Aviso de Reinicialização",
                    "description", "O servidor será reiniciado em breve!",
                    "color", 16744448,
                    "fields", List.of(
                        Map.of(
                            "name", "⏰ Tempo Restante",
                            "value", timeRemaining,
                            "inline", true
                        )
                    ),
                    "timestamp", timestamp,
                    "footer", Map.of(
                        "text", "Apocalipse [BR] - Zomboid Server"
                    )
                )
            )
        );
    }

    private Map<String, Object> buildInitiatedMessage() {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        
        return Map.of(
            "embeds", List.of(
                Map.of(
                    "title", "🔄 Reinicialização Iniciada!",
                    "description", "O processo de reinicialização do servidor foi iniciado.",
                    "color", 3447003,
                    "fields", List.of(
                        Map.of(
                            "name", "⏳ Duração Total",
                            "value", "10 minutos",
                            "inline", true
                        ),
                        Map.of(
                            "name", "📢 Avisos",
                            "value", "10min, 5min, 1min, 15s",
                            "inline", true
                        )
                    ),
                    "timestamp", timestamp,
                    "footer", Map.of(
                        "text", "Apocalipse [BR] - Zomboid Server"
                    )
                )
            )
        );
    }

    private Map<String, Object> buildShutdownMessage() {
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        
        return Map.of(
            "embeds", List.of(
                Map.of(
                    "title", "🛑 Servidor Desligando",
                    "description", "O servidor está sendo desligado agora. O servidor voltará online automaticamente após a reinicialização da VM.",
                    "color", 15158332,
                    "timestamp", timestamp,
                    "footer", Map.of(
                        "text", "Apocalipse [BR] - Zomboid Server"
                    )
                )
            )
        );
    }

    private void sendWebhook(Map<String, Object> payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("User-Agent", "Apocalipse-BR-Manager/1.0");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(
                webhookUrl,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.fine("Discord webhook sent successfully: " + response.getStatusCode());
            } else {
                logger.warning("Discord webhook returned status: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            logger.warning("Failed to send webhook: " + e.getMessage());
            throw e;
        }
    }

    public boolean isEnabled() {
        return notificationsEnabled && webhookUrl != null && !webhookUrl.isEmpty();
    }
}