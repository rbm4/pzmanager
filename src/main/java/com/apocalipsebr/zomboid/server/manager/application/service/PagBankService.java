package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.PagBankOrderDTO;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.PagBankOrderParamsDTO;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.PagbankNotificationReturnDTO;
import com.google.gson.Gson;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to interact with PagBank API for PIX QR Code generation and order
 * status checks.
 */
@Service
public class PagBankService {

    private static final Logger logger = LoggerFactory.getLogger(PagBankService.class);
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    @Value("${pagbank.url}")
    private String apiUrl;

    @Value("${pagbank.token}")
    private String token;

    @Value("${pagbank.notification-url}")
    private String notificationUrl;

    @Value("${pagbank.email}")
    private String email;

    @Value("${pagbank.qr-expiration-minutes}")
    private int qrExpirationMinutes;

    private final Gson gson = new Gson();
    private final OkHttpClient httpClient;

    public PagBankService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Creates a new PIX order on PagBank and returns the order response with QR
     * code data.
     *
     * @param referenceId    internal reference ID for the donation
     * @param amountCentavos amount in BRL centavos
     * @param customerName   the customer/donor name
     * @return PagBankOrderDTO with QR code info
     * @throws IOException if the API call fails
     */
    public PagBankOrderDTO createPixOrder(String referenceId, int amountCentavos, User user, String email, String cpf)
            throws IOException {
        // Build the item
        var item = new PagBankOrderParamsDTO.Item("Doação Apocalipse [BR]", 1, amountCentavos);
        var items = new ArrayList<PagBankOrderParamsDTO.Item>();
        items.add(item);

        // Build QR code with expiration
        String expirationDate = formatExpirationDate(qrExpirationMinutes);
        var qrAmount = new PagBankOrderParamsDTO.QrCode.Amount(amountCentavos);
        var qrCode = new PagBankOrderParamsDTO.QrCode(qrAmount, expirationDate);
        var qrCodes = new ArrayList<PagBankOrderParamsDTO.QrCode>();
        qrCodes.add(qrCode);

        // Build notification URLs
        var webhookUrls = new ArrayList<String>();
        webhookUrls.add(notificationUrl);

        // Build customer with user-provided email and CPF
        var customer = new PagBankOrderParamsDTO.Customer(user.getUsername(), email, cpf);

        // Build the request DTO
        var params = new PagBankOrderParamsDTO();
        params.setReference_id(referenceId);
        params.setCustomer(customer);
        params.setItems(items);
        params.setQr_codes(qrCodes);
        params.setNotification_urls(webhookUrls);

        String json = gson.toJson(params);
        logger.info("Creating PagBank PIX order: {}", json);

        Response response = postRequest(json, "orders");
        String responseBody = response.body() != null ? response.body().string() : "";
        logger.info("PagBank order response: {}", responseBody);

        if (!response.isSuccessful()) {
            throw new IOException("PagBank API error (" + response.code() + "): " + responseBody);
        }

        return gson.fromJson(responseBody, PagBankOrderDTO.class);
    }

    /**
     * Gets the current status of a PagBank order.
     *
     * @param orderId the PagBank order ID
     * @return PagBankOrderDTO with current status
     * @throws IOException if the API call fails
     */
    public PagBankOrderDTO getOrder(String orderId) throws IOException {
        Response response = getRequest("orders/" + orderId);
        String responseBody = response.body() != null ? response.body().string() : "";

        if (!response.isSuccessful()) {
            throw new IOException("PagBank API error (" + response.code() + "): " + responseBody);
        }

        return gson.fromJson(responseBody, PagBankOrderDTO.class);
    }

    private Response postRequest(String jsonBody, String path) throws IOException {
        String fullUrl = apiUrl + path;
        RequestBody body = RequestBody.create(jsonBody, JSON_MEDIA_TYPE);

        Request request = new Request.Builder()
                .url(fullUrl)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(body)
                .build();

        return httpClient.newCall(request).execute();
    }

    private Response getRequest(String path) throws IOException {
        String fullUrl = apiUrl + path;

        Request request = new Request.Builder()
                .url(fullUrl)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .get()
                .build();

        return httpClient.newCall(request).execute();
    }

    /**
     * Formats the expiration date as ISO 8601 with UTC-3 offset for PagBank API.
     */
    private String formatExpirationDate(int minutesFromNow) {
        OffsetDateTime expiration = OffsetDateTime.now(ZoneOffset.ofHours(-3))
                .plusMinutes(minutesFromNow);
        return expiration.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

    /**
     * Fetches a transaction/notification from PagBank's legacy notification API.
     * URL:
     * https://ws.pagseguro.uol.com.br/v3/transactions/notifications/{code}?email=X&token=Y
     *
     * @param notificationCode the notification code received from PagBank webhook
     * @return PagbankNotificationReturnDTO with transaction details
     * @throws IOException if the API call fails
     */
    public PagbankNotificationReturnDTO getNotification(String notificationCode, int counter) throws IOException {
        if (counter == 5)
            return null;
        String url = "https://ws.pagseguro.uol.com.br/v3/transactions/notifications/"
                + notificationCode + "?email=" + email + "&token=" + token;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/xml")
                .addHeader("Accept", "application/xml")
                .get()
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body() != null ? response.body().string() : "";
        logger.info("PagBank notification response ({}): {}", response.code(), responseBody);

        if (!response.isSuccessful()) {
            throw new IOException("PagBank notification API error (" + response.code() + "): " + responseBody);
        }
        PagbankNotificationReturnDTO transaction = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PagbankNotificationReturnDTO.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(responseBody);
            transaction = (PagbankNotificationReturnDTO) jaxbUnmarshaller
                    .unmarshal(reader);
        } catch (Exception e) {
            counter++;
            if (e.getClass() == SocketTimeoutException.class)
                return getNotification(notificationCode, counter);
            System.out.println(e.getMessage());
        }
        return transaction;
    }

}
