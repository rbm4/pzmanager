package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AwsProxyConfig {

    @Bean
    @ConfigurationProperties(prefix = "aws.proxy")
    public ProxyProperties proxyProperties() {
        return new ProxyProperties();
    }

    @Bean
    public Ec2Client ec2Client(ProxyProperties props) {
        if (props.getAccessKey() == null || props.getAccessKey().isBlank()
                || props.getSecretKey() == null || props.getSecretKey().isBlank()) {
            return null;
        }
        return Ec2Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())))
                .build();
    }

    public static class ProxyProperties {
        private boolean enabled = false;
        private String accessKey;
        private String secretKey;
        private String region = "sa-east-1";
        private Map<String, String> instances = new HashMap<>();
        private Map<String, String> addresses = new HashMap<>();
        private Map<String, String> names = new HashMap<>();
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

        public Map<String, String> getInstances() {
            return instances;
        }

        public void setInstances(Map<String, String> instances) {
            this.instances = instances;
        }

        public Map<String, String> getAddresses() {
            return addresses;
        }

        public void setAddresses(Map<String, String> addresses) {
            this.addresses = addresses;
        }

        public Map<String, String> getNames() {
            return names;
        }

        public void setNames(Map<String, String> names) {
            this.names = names;
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
