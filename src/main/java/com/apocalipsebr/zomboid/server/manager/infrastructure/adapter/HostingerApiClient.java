package com.apocalipsebr.zomboid.server.manager.infrastructure.adapter;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;

import com.apocalipsebr.zomboid.server.manager.infrastructure.config.HostingerConfig.HostingerProperties;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class HostingerApiClient {

    private static final Logger logger = Logger.getLogger(HostingerApiClient.class.getName());
    private static final String BASE_URL = "https://developers.hostinger.com";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    private final HostingerProperties hostingerProperties;
    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;

    public HostingerApiClient(HostingerProperties hostingerProperties) {
        this.hostingerProperties = hostingerProperties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String getDnsRecords(String domain) throws IOException {
        if (!hostingerProperties.isConfigured()) {
            throw new IOException("Hostinger API key not configured");
        }

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/dns/v1/zones/" + domain)
                .addHeader("Authorization", "Bearer " + hostingerProperties.getApiKey())
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                logger.warning("Hostinger DNS GET failed [" + response.code() + "]: " + body);
                throw new IOException("Hostinger API error " + response.code() + ": " + body);
            }
            return body;
        }
    }

    public void updateDnsRecord(String domain, String subdomain, String ip, int ttl) throws IOException {
        if (!hostingerProperties.isConfigured()) {
            throw new IOException("Hostinger API key not configured");
        }

        // Build zone payload:
        // { "overwrite": true, "zone": [{ "name": "proxy-sp", "records": [{"content":
        // "1.2.3.4"}], "ttl": 300, "type": "A" }] }
        JsonObject record = new JsonObject();
        record.addProperty("content", ip);

        JsonArray records = new JsonArray();
        records.add(record);

        JsonObject zoneEntry = new JsonObject();
        zoneEntry.addProperty("name", subdomain);
        zoneEntry.add("records", records);
        zoneEntry.addProperty("ttl", ttl);
        zoneEntry.addProperty("type", "A");

        JsonArray zone = new JsonArray();
        zone.add(zoneEntry);

        JsonObject payload = new JsonObject();
        payload.addProperty("overwrite", true);
        payload.add("zone", zone);

        String jsonBody = gson.toJson(payload);
        logger.info("Updating DNS: " + subdomain + "." + domain + " → " + ip + " (TTL " + ttl + ")");

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/dns/v1/zones/" + domain)
                .addHeader("Authorization", "Bearer " + hostingerProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .put(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                logger.warning("Hostinger DNS PUT failed [" + response.code() + "]: " + body);
                throw new IOException("Hostinger API error " + response.code() + ": " + body);
            }
            logger.info("DNS updated successfully: " + subdomain + "." + domain + " → " + ip);
        }
    }

    public void deleteDnsRecord(String domain, String subdomain) throws IOException {
        if (!hostingerProperties.isConfigured()) {
            throw new IOException("Hostinger API key not configured");
        }

        // To "delete" an A record, we overwrite with an empty content pointing nowhere
        // Hostinger's API doesn't have per-record DELETE, so we update with the zone
        // minus this record
        // For simplicity: update the record to point to 0.0.0.0 (effectively dead)
        // A proper delete would require GET → filter → PUT, but this is safer
        logger.info("Clearing DNS record: " + subdomain + "." + domain);

        // GET current records, filter out the target subdomain, PUT back
        String currentRecordsJson = getDnsRecords(domain);
        JsonArray currentZone = gson.fromJson(currentRecordsJson, JsonArray.class);

        JsonArray filteredZone = new JsonArray();
        for (var element : currentZone) {
            JsonObject entry = element.getAsJsonObject();
            String name = entry.get("name").getAsString();
            String type = entry.has("type") ? entry.get("type").getAsString() : "";
            if (!(name.equals(subdomain) && "A".equals(type))) {
                filteredZone.add(entry);
            }
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("overwrite", true);
        payload.add("zone", filteredZone);

        String jsonBody = gson.toJson(payload);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/dns/v1/zones/" + domain)
                .addHeader("Authorization", "Bearer " + hostingerProperties.getApiKey())
                .addHeader("Content-Type", "application/json")
                .put(RequestBody.create(jsonBody, JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                logger.warning("Hostinger DNS delete failed [" + response.code() + "]: " + body);
                throw new IOException("Hostinger API error " + response.code() + ": " + body);
            }
            logger.info("DNS record cleared: " + subdomain + "." + domain);
        }
    }

    public boolean isConfigured() {
        return hostingerProperties.isConfigured();
    }
}
