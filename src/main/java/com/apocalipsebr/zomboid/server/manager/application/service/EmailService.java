package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.infrastructure.config.EmailConfig.EmailProperties;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    private final EmailProperties emailProperties;

    public EmailService(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }

    public void sendTextEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email cannot be empty");
        }
        sendTextEmail(List.of(to), subject, body);
    }

    public void sendTextEmail(List<String> to, String subject, String body) {
        if (!emailProperties.isEnabled()) {
            logger.fine("Email sending is disabled");
            return;
        }

        if (!emailProperties.isConfigured()) {
            logger.warning("Email is enabled but credentials/config are missing");
            return;
        }

        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required");
        }

        try {
            Session session = Session.getInstance(buildMailProperties(), new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailProperties.getUsername(), emailProperties.getPassword());
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailProperties.resolveFromAddress()));
            message.setRecipients(Message.RecipientType.TO, parseRecipients(to));
            message.setSubject(subject != null ? subject : "");
            message.setText(body != null ? body : "");

            Transport.send(message);
            logger.info("Email sent to: " + String.join(",", to));
        } catch (MessagingException e) {
            logger.log(Level.SEVERE, "Failed to send email", e);
            throw new IllegalStateException("Failed to send email", e);
        }
    }

    public boolean isEnabledAndConfigured() {
        return emailProperties.isEnabled() && emailProperties.isConfigured();
    }

    private Properties buildMailProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", emailProperties.getSmtp().getHost());
        props.put("mail.smtp.port", String.valueOf(emailProperties.getSmtp().getPort()));
        props.put("mail.smtp.auth", String.valueOf(emailProperties.getSmtp().isAuth()));
        props.put("mail.smtp.ssl.enable", String.valueOf(emailProperties.getSmtp().isSslEnable()));
        return props;
    }

    private InternetAddress[] parseRecipients(List<String> recipients) throws MessagingException {
        String joinedRecipients = recipients.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .reduce((left, right) -> left + "," + right)
                .orElseThrow(() -> new IllegalArgumentException("At least one valid recipient is required"));

        return InternetAddress.parse(joinedRecipients);
    }
}