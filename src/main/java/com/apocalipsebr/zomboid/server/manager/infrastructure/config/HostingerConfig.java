package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HostingerConfig {

    @Bean
    @ConfigurationProperties(prefix = "hostinger")
    public HostingerProperties hostingerProperties() {
        return new HostingerProperties();
    }

    public static class HostingerProperties {
        private String apiKey;
        private String baseDomain = "apocalipse.cloud";
        private int dnsTtl = 300;

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getBaseDomain() { return baseDomain; }
        public void setBaseDomain(String baseDomain) { this.baseDomain = baseDomain; }

        public int getDnsTtl() { return dnsTtl; }
        public void setDnsTtl(int dnsTtl) { this.dnsTtl = dnsTtl; }

        public boolean isConfigured() {
            return apiKey != null && !apiKey.isBlank();
        }
    }
}
