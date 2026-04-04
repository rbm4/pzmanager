package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsProxyConfig {

    @Bean
    @ConfigurationProperties(prefix = "aws.proxy")
    public ProxyProperties proxyProperties() {
        return new ProxyProperties();
    }

    public static class ProxyProperties {
        private boolean enabled = false;
        private String accessKey;
        private String secretKey;
        private String region = "sa-east-1";
        private int creditsPer24h = 4000;
        private int minHours = 6;
        private int maxHours = 36;
        private int hourStep = 6;
        private int pollIntervalSeconds = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public int getCreditsPer24h() {
            return creditsPer24h;
        }

        public void setCreditsPer24h(int creditsPer24h) {
            this.creditsPer24h = creditsPer24h;
        }

        public int getMinHours() {
            return minHours;
        }

        public void setMinHours(int minHours) {
            this.minHours = minHours;
        }

        public int getMaxHours() {
            return maxHours;
        }

        public void setMaxHours(int maxHours) {
            this.maxHours = maxHours;
        }

        public int getHourStep() {
            return hourStep;
        }

        public void setHourStep(int hourStep) {
            this.hourStep = hourStep;
        }

        public int getPollIntervalSeconds() {
            return pollIntervalSeconds;
        }

        public void setPollIntervalSeconds(int pollIntervalSeconds) {
            this.pollIntervalSeconds = pollIntervalSeconds;
        }

        public int calculateCredits(int hours) {
            return (int) Math.ceil((double) hours / 24.0 * creditsPer24h);
        }

        public boolean isValidHours(int hours) {
            return hours >= minHours && hours <= maxHours && hours % hourStep == 0;
        }
    }
}
