package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class LetsCloudProxyProvider implements ProxyProvider {

    private static final Logger logger = Logger.getLogger(LetsCloudProxyProvider.class.getName());
    public static final String PROVIDER_TYPE = "LETSCLOUD";
    private static final String BASE_URL = "https://core.letscloud.io/api";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    private final String apiKey;
    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;

    public LetsCloudProxyProvider(@Value("${letscloud.api-key:}") String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(60))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Override
    public String getProviderType() {
        return PROVIDER_TYPE;
    }

    @Override
    public void startInstance(String instanceId, String region) {
        if (!isConfigured()) {
            logger.warning("LetsCloud not configured — skipping startInstance for " + instanceId);
            return;
        }
        logger.info("Starting LetsCloud instance: " + instanceId);
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/instances/" + instanceId + "/power-on")
                    .addHeader("api-token", apiKey)
                    .put(RequestBody.create("", JSON_MEDIA_TYPE))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    logger.warning("LetsCloud power-on failed [" + response.code() + "]: " + body);
                } else {
                    logger.info("LetsCloud power-on success for " + instanceId);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to start LetsCloud instance " + instanceId, e);
        }
    }

    @Override
    public void stopInstance(String instanceId, String region) {
        if (!isConfigured()) {
            logger.warning("LetsCloud not configured — skipping stopInstance for " + instanceId);
            return;
        }
        logger.info("Stopping LetsCloud instance: " + instanceId);
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/instances/" + instanceId + "/power-off")
                    .addHeader("api-token", apiKey)
                    .put(RequestBody.create("", JSON_MEDIA_TYPE))
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    logger.warning("LetsCloud power-off failed [" + response.code() + "]: " + body);
                } else {
                    logger.info("LetsCloud power-off success for " + instanceId);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to stop LetsCloud instance " + instanceId, e);
        }
    }

    @Override
    public ProxyInstanceState getInstanceState(String instanceId, String region) {
        if (!isConfigured()) {
            logger.warning("LetsCloud not configured — returning 'stopped' for " + instanceId);
            return new ProxyInstanceState("stopped", null);
        }
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/instances/" + instanceId)
                    .addHeader("api-token", apiKey)
                    .get()
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    logger.warning("LetsCloud get instance failed [" + response.code() + "]: " + body);
                    return new ProxyInstanceState("unknown", null);
                }

                JsonObject json = gson.fromJson(body, JsonObject.class);
                if (!json.has("success") || !json.get("success").getAsBoolean()) {
                    String msg = json.has("message") ? json.get("message").getAsString() : "unknown error";
                    logger.warning("LetsCloud get instance error: " + msg);
                    return new ProxyInstanceState("unknown", null);
                }

                JsonObject data = json.getAsJsonObject("data");

                // Extract first IP address
                String publicIp = null;
                if (data.has("ip_addresses")) {
                    JsonArray ipAddresses = data.getAsJsonArray("ip_addresses");
                    if (!ipAddresses.isEmpty()) {
                        JsonObject firstIp = ipAddresses.get(0).getAsJsonObject();
                        if (firstIp.has("address")) {
                            publicIp = firstIp.get("address").getAsString();
                        }
                    }
                }

                // LetsCloud API returns the same response for powered-on and powered-off machines,
                // so we ping the IP to determine actual reachability
                String state;
                if (publicIp != null && !publicIp.isBlank()) {
                    boolean reachable = isReachable(publicIp);
                    state = reachable ? "running" : "stopped";
                    logger.info("LetsCloud instance " + instanceId + " ping " + publicIp
                            + " → " + (reachable ? "reachable" : "unreachable") + " → state: " + state);
                } else {
                    state = "unknown";
                    logger.warning("LetsCloud instance " + instanceId + " has no IP address — cannot determine state");
                }
                return new ProxyInstanceState(state, publicIp);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to get LetsCloud instance state " + instanceId, e);
            return new ProxyInstanceState("unknown", null);
        }
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Pings the host using the OS ping command (3 packets, 2s timeout).
     * Returns true if at least one reply is received (exit code 0).
     */
    private boolean isReachable(String host) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("ping", "-n", "3", "-w", "2000", host);
            } else {
                pb = new ProcessBuilder("ping", "-c", "3", "-W", "2", host);
            }
            pb.redirectErrorStream(true);
            Process process = pb.start();
            // Consume output to prevent blocking
            try (InputStream is = process.getInputStream()) {
                is.readAllBytes();
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ping failed for " + host, e);
            return false;
        }
    }
}
