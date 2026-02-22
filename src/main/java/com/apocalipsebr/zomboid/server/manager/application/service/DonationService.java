package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Donation;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.DonationRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ZomboidItemRepository;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.DonationStatusDTO;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.PagBankOrderDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Service to handle donation lifecycle: creation, status checks, and coin
 * crediting.
 */
@Service
public class DonationService {

    private static final Logger logger = LoggerFactory.getLogger(DonationService.class);

    /**
     * The item ID used as reference for dynamic coin rate calculation.
     * This item is assumed to cost R$10.00 (1000 centavos).
     * Rate per centavo = item.value / 1000.
     */
    private static final String REFERENCE_ITEM_ID = "Base.SkillRecoveryBoundJournalFull";
    private static final int REFERENCE_ITEM_CENTAVOS = 1000; // R$10.00
    private static final int DEFAULT_COIN_RATE = 15; // fallback coins per centavo

    @Value("${pagbank.qr-expiration-minutes:5}")
    private int qrExpirationMinutes;

    private final PagBankService pagBankService;
    private final DonationRepository donationRepository;
    private final CharacterRepository characterRepository;
    private final ZomboidItemRepository zomboidItemRepository;
    private final UserRepository userRepository;
    private final TransactionLogService transactionLogService;

    public DonationService(PagBankService pagBankService,
            DonationRepository donationRepository,
            CharacterRepository characterRepository,
            ZomboidItemRepository zomboidItemRepository,
            UserRepository userRepository,
            TransactionLogService transactionLogService) {
        this.pagBankService = pagBankService;
        this.donationRepository = donationRepository;
        this.characterRepository = characterRepository;
        this.zomboidItemRepository = zomboidItemRepository;
        this.userRepository = userRepository;
        this.transactionLogService = transactionLogService;
    }

    /**
     * Dynamically calculates the coin rate per centavo based on the reference item.
     * The reference item (Base.SkillRecoveryBoundJournalFull) is assumed to cost
     * R$10.00.
     * Rate = item.value (coins) / 1000 (centavos in R$10).
     * Falls back to DEFAULT_COIN_RATE if item not found or not sellable.
     */
    public int getCoinRatePerCentavo() {
        try {
            Optional<ZomboidItem> itemOpt = zomboidItemRepository.findByItemId(REFERENCE_ITEM_ID);
            if (itemOpt.isPresent()) {
                ZomboidItem item = itemOpt.get();
                if (item.getSellable() && item.getValue() != null && item.getValue() > 0) {
                    return item.getValue() / REFERENCE_ITEM_CENTAVOS;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to calculate dynamic coin rate, using default", e);
        }
        return DEFAULT_COIN_RATE;
    }

    /**
     * Creates a new PIX donation for the user.
     * Uses the latest (most recently updated) character for coin crediting.
     *
     * @param user           the logged-in user
     * @param amountCentavos donation amount in BRL centavos
     * @param email          customer email for PagBank
     * @param cpf            customer CPF for PagBank
     * @param rememberInfo   whether to save email/cpf to user profile
     * @return DonationStatusDTO with QR code info
     * @throws IOException           if PagBank API fails
     * @throws IllegalStateException if user has no characters
     */
    @Transactional
    public DonationStatusDTO createDonation(User user, int amountCentavos, String email, String cpf,
            boolean rememberInfo) throws IOException {
        // Find the latest character (by lastUpdate)
        List<Character> characters = characterRepository.findByUser(user);
        if (characters.isEmpty()) {
            throw new IllegalStateException("Você precisa ter pelo menos um personagem para fazer uma doação.");
        }

        Character latestCharacter = characters.stream()
                .max((a, b) -> {
                    LocalDateTime aTime = a.getLastUpdate() != null ? a.getLastUpdate() : a.getCreatedAt();
                    LocalDateTime bTime = b.getLastUpdate() != null ? b.getLastUpdate() : b.getCreatedAt();
                    return aTime.compareTo(bTime);
                })
                .orElseThrow();

        // Calculate coins dynamically based on reference item price
        int coinRate = getCoinRatePerCentavo();
        int coinsToAward = amountCentavos * coinRate;

        // Create donation record (PENDING)
        Donation donation = new Donation();
        donation.setUser(user);
        donation.setCharacter(latestCharacter);
        donation.setAmountCentavos(amountCentavos);
        donation.setCoinsAwarded(coinsToAward);
        donation.setExpiresAt(LocalDateTime.now().plusMinutes(qrExpirationMinutes));
        donationRepository.save(donation);
        // Flush to ensure ID is generated
        donationRepository.flush();

        // Save email/cpf to user profile if "remember" is checked
        if (rememberInfo) {
            user.setPagbankEmail(email);
            user.setPagbankCpf(cpf);
            userRepository.save(user);
        }

        // Create PagBank PIX order
        try {
            PagBankOrderDTO order = pagBankService.createPixOrder(
                    "DONATION-" + donation.getId(),
                    amountCentavos,
                    user,
                    email,
                    cpf);

            if (order.getError_messages() != null && !order.getError_messages().isEmpty()) {
                donation.setStatus("FAILED");
                donationRepository.save(donation);
                String errors = order.getError_messages().stream()
                        .map(e -> e.getCode() + ": " + e.getDescription())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse("Unknown error");
                throw new IOException("PagBank rejected order: " + errors);
            }

            // Save PagBank data
            donation.setPagbankOrderId(order.getId());

            if (order.getQr_codes() != null && !order.getQr_codes().isEmpty()) {
                PagBankOrderDTO.QrCode qrCode = order.getQr_codes().get(0);
                donation.setPixCopyPaste(qrCode.getText());

                // Extract QR code image URL from links
                if (qrCode.getLinks() != null) {
                    qrCode.getLinks().stream()
                            .filter(link -> "image/png".equals(link.getType()) || "QRCODE.PNG".equals(link.getRel()))
                            .findFirst()
                            .ifPresent(link -> donation.setQrCodeImageUrl(link.getHref()));
                }
            }

            donationRepository.save(donation);
            return buildStatusDTO(donation);

        } catch (IOException e) {
            donation.setStatus("FAILED");
            donationRepository.save(donation);
            throw e;
        }
    }

    /**
     * Checks and updates donation status by polling PagBank.
     * If paid, credits coins to the character.
     *
     * @param donationId the donation ID
     * @param user       the user requesting the check (for ownership validation)
     * @return updated DonationStatusDTO
     */
    @Transactional
    public DonationStatusDTO checkDonationStatus(Long donationId, User user) {
        Donation donation = donationRepository.findByIdEager(donationId)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada."));

        // Validate ownership
        if (!donation.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Acesso negado.");
        }

        // If already in terminal state, just return
        if (!"PENDING".equals(donation.getStatus())) {
            return buildStatusDTO(donation);
        }

        // Check if expired locally
        if (LocalDateTime.now().isAfter(donation.getExpiresAt())) {
            donation.setStatus("EXPIRED");
            donationRepository.save(donation);
            return buildStatusDTO(donation);
        }

        // Poll PagBank for status
        if (donation.getPagbankOrderId() != null) {
            try {
                PagBankOrderDTO order = pagBankService.getOrder(donation.getPagbankOrderId());
                if (order.isPaid()) {
                    return creditDonation(donation);
                }
            } catch (IOException e) {
                logger.warn("Failed to check PagBank order status for donation {}", donationId, e);
                // Don't fail the status check, just return current state
            }
        }

        return buildStatusDTO(donation);
    }

    /**
     * Webhook handler: processes PagBank notification callback.
     * 
     * @param notificationType
     */
    @Transactional
    public void processWebhook(String notificationCode, String notificationType) {
        logger.info("Notificação PagBank recebida - code: {}, type: {}", notificationCode, notificationType);

        if (!"transaction".equals(notificationType)) {
            logger.info("Ignoring non-transaction notification type: {}", notificationType);
            return;
        }

        try {
            var notification = pagBankService.getNotification(notificationCode,0);
            String reference = notification.reference();
            String status = notification.status();

            logger.info("PagBank notification - reference: {}, status: {}", reference, status);

            if (reference == null || !reference.startsWith("DONATION-")) {
                logger.warn("Unknown reference in PagBank notification: {}", reference);
                return;
            }

            Long donationId = Long.parseLong(reference.replace("DONATION-", ""));
            Optional<Donation> donationOpt = donationRepository.findById(donationId);

            if (donationOpt.isPresent()) {
                Donation donation = donationOpt.get();
                // PagBank status "3" = Paga, "4" = Disponível
                if ("PENDING".equals(donation.getStatus()) && ("3".equals(status) || "4".equals(status))) {
                    donation.setPagbankOrderId(notification.code());
                    creditDonation(donation);
                    logger.info("Donation {} credited via webhook notification", donationId);
                }
            }
        } catch (Exception e) {
            logger.warn("Error processing PagBank webhook notification", e);
        }
    }

    /**
     * Credits coins to the character and marks donation as PAID.
     */
    private DonationStatusDTO creditDonation(Donation donation) {
        donation.setStatus("PAID");
        donation.setPaidAt(LocalDateTime.now());

        // Credit coins to character
        Character character = donation.getCharacter();
        int newBalance = character.getCurrencyPoints() + donation.getCoinsAwarded();
        character.setCurrencyPoints(newBalance);
        characterRepository.save(character);

        donationRepository.save(donation);

        // Log the transaction
        transactionLogService.logTransaction(
                donation.getUser(),
                character,
                "DONATION",
                "Doação PIX - R$" + String.format("%.2f", donation.getAmountCentavos() / 100.0),
                "PAGBANK-" + donation.getPagbankOrderId(),
                donation.getCoinsAwarded(),
                newBalance);

        logger.info("Donation {} PAID - {} coins credited to character {}",
                donation.getId(), donation.getCoinsAwarded(), character.getPlayerName());

        return buildStatusDTO(donation);
    }

    private DonationStatusDTO buildStatusDTO(Donation donation) {
        DonationStatusDTO dto = new DonationStatusDTO();
        dto.setDonationId(donation.getId());
        dto.setStatus(donation.getStatus());
        dto.setAmountCentavos(donation.getAmountCentavos());
        dto.setCoinsAwarded(donation.getCoinsAwarded());
        dto.setPixCopyPaste(donation.getPixCopyPaste());
        dto.setQrCodeImageUrl(donation.getQrCodeImageUrl());
        dto.setCharacterName(donation.getCharacter().getPlayerName());

        // Calculate remaining seconds
        if ("PENDING".equals(donation.getStatus())) {
            long secondsRemaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), donation.getExpiresAt());
            dto.setExpiresInSeconds(Math.max(0, secondsRemaining));
        } else {
            dto.setExpiresInSeconds(0);
        }

        return dto;
    }
}
