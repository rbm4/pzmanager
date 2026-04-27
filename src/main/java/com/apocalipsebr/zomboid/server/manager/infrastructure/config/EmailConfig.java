package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Bean
    @ConfigurationProperties(prefix = "email")
    public EmailProperties emailProperties() {
        return new EmailProperties();
    }

    public static class EmailProperties {
        private boolean enabled;
        private String username;
        private String password;
        private String from;
        private final Smtp smtp = new Smtp();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public Smtp getSmtp() {
            return smtp;
        }

        public String resolveFromAddress() {
            if (from != null && !from.isBlank()) {
                return from;
            }
            return username;
        }

        public boolean isConfigured() {
            return username != null && !username.isBlank()
                    && password != null && !password.isBlank()
                    && smtp.getHost() != null && !smtp.getHost().isBlank();
        }
    }

    public static class Smtp {
        private String host = "smtp.hostinger.com";
        private int port = 465;
        private boolean auth = true;
        private boolean sslEnable = true;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isAuth() {
            return auth;
        }

        public void setAuth(boolean auth) {
            this.auth = auth;
        }

        public boolean isSslEnable() {
            return sslEnable;
        }

        public void setSslEnable(boolean sslEnable) {
            this.sslEnable = sslEnable;
        }
    }
}