package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ProxyActivation;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ProxyActivationRepository;
import com.apocalipsebr.zomboid.server.manager.infrastructure.adapter.Ec2ProxyManager;
import com.apocalipsebr.zomboid.server.manager.infrastructure.config.AwsProxyConfig.ProxyProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ProxyService {

    private static final Logger logger = Logger.getLogger(ProxyService.class.getName());
    private static final int STARTING_TIMEOUT_MINUTES = 5;

    private final ProxyActivationRepository proxyActivationRepository;
    private final Ec2ProxyManager ec2ProxyManager;
    private final ProxyProperties proxyProperties;
    private final CharacterService characterService;
    private final TransactionLogService transactionLogService;

    private final Object activationLock = new Object();

    public ProxyService(ProxyActivationRepository proxyActivationRepository,
                        Ec2ProxyManager ec2ProxyManager,
                        ProxyProperties proxyProperties,
                        CharacterService characterService,
                        TransactionLogService transactionLogService) {
        this.proxyActivationRepository = proxyActivationRepository;
        this.ec2ProxyManager = ec2ProxyManager;
        this.proxyProperties = proxyProperties;
        this.characterService = characterService;
        this.transactionLogService = transactionLogService;
    }

    // ==================== ACTIVATE ====================

    public record ActivateResult(boolean success, String error, int errorCode, ProxyActivation activation) {
        public static ActivateResult ok(ProxyActivation activation) {
            return new ActivateResult(true, null, 0, activation);
        }
        public static ActivateResult fail(String error, int errorCode) {
            return new ActivateResult(false, error, errorCode, null);
        }
    }

    @Transactional
    public ActivateResult activate(User user, String proxyId, int hours) {
        // Validate hours
        if (!proxyProperties.isValidHours(hours)) {
            return ActivateResult.fail("Invalid duration. Must be " + proxyProperties.getMinHours()
                    + "-" + proxyProperties.getMaxHours() + "h in " + proxyProperties.getHourStep() + "h increments.", 400);
        }

        // Validate proxy ID exists in config
        String instanceId = proxyProperties.getInstances().get(proxyId);
        if (instanceId == null || instanceId.isBlank()) {
            return ActivateResult.fail("Unknown proxy region: " + proxyId, 400);
        }

        int creditsCost = proxyProperties.calculateCredits(hours);

        synchronized (activationLock) {
            // Check if proxy is already active
            Optional<ProxyActivation> existing = proxyActivationRepository.findByProxyIdAndStatusIn(
                    proxyId, List.of(ProxyActivation.STATUS_ACTIVE, ProxyActivation.STATUS_STARTING));
            if (existing.isPresent()) {
                return ActivateResult.fail("Proxy already active. Expires at: " + existing.get().getExpiresAt(), 409);
            }

            // Check credits
            List<Character> userCharacters = characterService.getAllUserCharacters(user);
            int totalCurrency = userCharacters.stream()
                    .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                    .sum();

            if (totalCurrency < creditsCost) {
                return ActivateResult.fail("Insufficient credits. Required: " + creditsCost
                        + " ₳, Available: " + totalCurrency + " ₳", 402);
            }

            // Deduct credits (drain pattern from SoftWipeService)
            int remainingCost = creditsCost;
            for (Character character : userCharacters) {
                if (remainingCost <= 0) break;
                int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
                if (currentPoints > 0) {
                    int deduction = Math.min(currentPoints, remainingCost);
                    character.setCurrencyPoints(currentPoints - deduction);
                    remainingCost -= deduction;

                    transactionLogService.logTransaction(
                            user, character, "PROXY_ACTIVATION",
                            "Proxy: " + proxyId + " (" + hours + "h)",
                            "proxy_" + proxyId,
                            deduction, character.getCurrencyPoints());

                    logger.info("Deducted " + deduction + " ₳ from character: " + character.getPlayerName()
                            + " for proxy activation " + proxyId);
                }
            }
            characterService.saveAll(userCharacters);

            // Create activation record
            ProxyActivation activation = new ProxyActivation(user, proxyId, instanceId, creditsCost, hours);
            activation = proxyActivationRepository.save(activation);

            // Start EC2 instance
            try {
                ec2ProxyManager.startInstance(instanceId);
                logger.info("Proxy activation #" + activation.getId() + " created by " + user.getUsername()
                        + " — " + proxyId + " for " + hours + "h, cost: " + creditsCost + " ₳");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to start EC2 instance " + instanceId, e);
                activation.setStatus(ProxyActivation.STATUS_FAILED);
                activation.setStoppedAt(LocalDateTime.now());
                proxyActivationRepository.save(activation);
                refundCredits(user, creditsCost, proxyId);
                return ActivateResult.fail("Failed to start proxy instance. Credits have been refunded.", 500);
            }

            return ActivateResult.ok(activation);
        }
    }

    // ==================== EXTEND ====================

    public record ExtendResult(boolean success, String error, int errorCode,
                               LocalDateTime newExpiresAt, int additionalCreditsSpent, int totalHours) {
        public static ExtendResult ok(LocalDateTime newExpiresAt, int additionalCreditsSpent, int totalHours) {
            return new ExtendResult(true, null, 0, newExpiresAt, additionalCreditsSpent, totalHours);
        }
        public static ExtendResult fail(String error, int errorCode) {
            return new ExtendResult(false, error, errorCode, null, 0, 0);
        }
    }

    @Transactional
    public ExtendResult extend(User user, Long activationId, int additionalHours) {
        if (!proxyProperties.isValidHours(additionalHours)) {
            return ExtendResult.fail("Invalid extension duration. Must be in " + proxyProperties.getHourStep() + "h increments.", 400);
        }

        Optional<ProxyActivation> opt = proxyActivationRepository.findById(activationId);
        if (opt.isEmpty()) {
            return ExtendResult.fail("Activation not found.", 404);
        }

        ProxyActivation activation = opt.get();

        if (!activation.getUser().getId().equals(user.getId())) {
            return ExtendResult.fail("Only the user who activated this proxy can extend it.", 403);
        }

        if (!ProxyActivation.STATUS_ACTIVE.equals(activation.getStatus())) {
            return ExtendResult.fail("Proxy must be ACTIVE to extend. Current status: " + activation.getStatus(), 400);
        }

        int newTotalHours = activation.getHours() + additionalHours;
        if (newTotalHours > proxyProperties.getMaxHours()) {
            return ExtendResult.fail("Total duration cannot exceed " + proxyProperties.getMaxHours()
                    + "h. Current: " + activation.getHours() + "h, requested: +" + additionalHours + "h.", 400);
        }

        int creditsCost = proxyProperties.calculateCredits(additionalHours);

        // Check and deduct credits
        List<Character> userCharacters = characterService.getAllUserCharacters(user);
        int totalCurrency = userCharacters.stream()
                .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                .sum();

        if (totalCurrency < creditsCost) {
            return ExtendResult.fail("Insufficient credits. Required: " + creditsCost
                    + " ₳, Available: " + totalCurrency + " ₳", 402);
        }

        // Deduct credits
        int remainingCost = creditsCost;
        for (Character character : userCharacters) {
            if (remainingCost <= 0) break;
            int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
            if (currentPoints > 0) {
                int deduction = Math.min(currentPoints, remainingCost);
                character.setCurrencyPoints(currentPoints - deduction);
                remainingCost -= deduction;

                transactionLogService.logTransaction(
                        user, character, "PROXY_ACTIVATION",
                        "Proxy extend: " + activation.getProxyId() + " (+" + additionalHours + "h)",
                        "proxy_extend_" + activation.getId(),
                        deduction, character.getCurrencyPoints());
            }
        }
        characterService.saveAll(userCharacters);

        // Update activation
        activation.setExpiresAt(activation.getExpiresAt().plusHours(additionalHours));
        activation.setHours(newTotalHours);
        activation.setCreditsSpent(activation.getCreditsSpent() + creditsCost);
        proxyActivationRepository.save(activation);

        logger.info("Proxy activation #" + activation.getId() + " extended by " + user.getUsername()
                + " — +" + additionalHours + "h (total " + newTotalHours + "h), cost: " + creditsCost + " ₳");

        return ExtendResult.ok(activation.getExpiresAt(), creditsCost, newTotalHours);
    }

    // ==================== STATUS ====================

    public record ProxyInfo(String proxyId, String region, String address, int port,
                            String status, String activatedBy, LocalDateTime expiresAt,
                            long remainingMinutes, Long activationId, int hours) {}

    public record StatusResult(List<ProxyInfo> proxies, int userCredits) {}

    @Transactional(propagation = Propagation.REQUIRED)
    public StatusResult getStatus(User user) {
        List<ProxyInfo> proxies = new ArrayList<>();

        for (Map.Entry<String, String> entry : proxyProperties.getInstances().entrySet()) {
            String proxyId = entry.getKey();
            String displayName = proxyProperties.getNames().getOrDefault(proxyId, proxyId);
            String address = proxyProperties.getAddresses().getOrDefault(proxyId, "");

            Optional<ProxyActivation> active = proxyActivationRepository.findByProxyIdAndStatusIn(
                    proxyId, List.of(ProxyActivation.STATUS_ACTIVE, ProxyActivation.STATUS_STARTING));

            if (active.isPresent()) {
                ProxyActivation a = active.get();
                long remaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), a.getExpiresAt());
                proxies.add(new ProxyInfo(proxyId, displayName, address, 16261,
                        a.getStatus(), a.getUser().getUsername(), a.getExpiresAt(),
                        Math.max(0, remaining), a.getId(), a.getHours()));
            } else {
                proxies.add(new ProxyInfo(proxyId, displayName, null, 16261,
                        "STOPPED", null, null, 0, null, 0));
            }
        }

        int userCredits = characterService.getTotalCurrency(user);
        return new StatusResult(proxies, userCredits);
    }

    // ==================== HISTORY ====================

    public Page<ProxyActivation> getHistory(User user, Pageable pageable) {
        return proxyActivationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    // ==================== EXPIRATION (called by poller) ====================

    @Transactional
    public void processExpiredActivations() {
        // 1. ACTIVE past expiry → STOPPING (call EC2 stop)
        List<ProxyActivation> expired = proxyActivationRepository
                .findByStatusAndExpiresAtBefore(ProxyActivation.STATUS_ACTIVE, LocalDateTime.now());
        for (ProxyActivation activation : expired) {
            try {
                logger.info("Proxy #" + activation.getId() + " (" + activation.getProxyId()
                        + ") expired — stopping EC2 instance " + activation.getInstanceId());
                ec2ProxyManager.stopInstance(activation.getInstanceId());
                activation.setStatus(ProxyActivation.STATUS_STOPPING);
                proxyActivationRepository.save(activation);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to stop EC2 instance " + activation.getInstanceId(), e);
            }
        }

        // 2. STOPPING → check EC2 state, if stopped → EXPIRED
        List<ProxyActivation> stopping = proxyActivationRepository.findByStatus(ProxyActivation.STATUS_STOPPING);
        for (ProxyActivation activation : stopping) {
            try {
                String state = ec2ProxyManager.getInstanceState(activation.getInstanceId());
                if ("stopped".equals(state)) {
                    activation.setStatus(ProxyActivation.STATUS_EXPIRED);
                    activation.setStoppedAt(LocalDateTime.now());
                    proxyActivationRepository.save(activation);
                    logger.info("Proxy #" + activation.getId() + " (" + activation.getProxyId() + ") confirmed stopped");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to check state for instance " + activation.getInstanceId(), e);
            }
        }

        // 3. STARTING older than 5 min → check EC2, promote to ACTIVE or mark FAILED
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(STARTING_TIMEOUT_MINUTES);
        List<ProxyActivation> staleStarting = proxyActivationRepository
                .findByStatusAndActivatedAtBefore(ProxyActivation.STATUS_STARTING, cutoff);
        for (ProxyActivation activation : staleStarting) {
            try {
                String state = ec2ProxyManager.getInstanceState(activation.getInstanceId());
                if ("running".equals(state)) {
                    activation.setStatus(ProxyActivation.STATUS_ACTIVE);
                    proxyActivationRepository.save(activation);
                    logger.info("Proxy #" + activation.getId() + " (" + activation.getProxyId() + ") promoted to ACTIVE");
                } else if ("stopped".equals(state) || "terminated".equals(state)) {
                    activation.setStatus(ProxyActivation.STATUS_FAILED);
                    activation.setStoppedAt(LocalDateTime.now());
                    proxyActivationRepository.save(activation);
                    refundCredits(activation.getUser(), activation.getCreditsSpent(), activation.getProxyId());
                    logger.warning("Proxy #" + activation.getId() + " FAILED (EC2 state: " + state + ") — credits refunded");
                }
                // else: still pending/shutting-down, wait for next cycle
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to check stale STARTING activation #" + activation.getId(), e);
            }
        }

        // 4. Check fresh STARTING activations (< 5 min) → if running, promote to ACTIVE
        List<ProxyActivation> freshStarting = proxyActivationRepository.findByStatus(ProxyActivation.STATUS_STARTING);
        for (ProxyActivation activation : freshStarting) {
            if (activation.getActivatedAt().isBefore(cutoff)) continue; // already handled above
            try {
                String state = ec2ProxyManager.getInstanceState(activation.getInstanceId());
                if ("running".equals(state)) {
                    activation.setStatus(ProxyActivation.STATUS_ACTIVE);
                    proxyActivationRepository.save(activation);
                    logger.info("Proxy #" + activation.getId() + " (" + activation.getProxyId() + ") promoted to ACTIVE");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to check STARTING activation #" + activation.getId(), e);
            }
        }
    }

    // ==================== RECONCILIATION (called on startup) ====================

    @Transactional
    public void reconcile() {
        if (!ec2ProxyManager.isConfigured()) {
            logger.info("EC2 not configured — skipping proxy reconciliation");
            return;
        }

        logger.info("Running proxy reconciliation...");

        // 1. ACTIVE activations — verify EC2 is actually running
        List<ProxyActivation> activeRecords = proxyActivationRepository.findByStatus(ProxyActivation.STATUS_ACTIVE);
        for (ProxyActivation activation : activeRecords) {
            try {
                String state = ec2ProxyManager.getInstanceState(activation.getInstanceId());
                if ("stopped".equals(state) || "terminated".equals(state)) {
                    logger.warning("Reconcile: Proxy #" + activation.getId() + " marked ACTIVE but EC2 is " + state
                            + " — marking EXPIRED");
                    activation.setStatus(ProxyActivation.STATUS_EXPIRED);
                    activation.setStoppedAt(LocalDateTime.now());
                    proxyActivationRepository.save(activation);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Reconcile: Failed to check activation #" + activation.getId(), e);
            }
        }

        // 2. Stale STARTING → FAILED with refund
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(STARTING_TIMEOUT_MINUTES);
        List<ProxyActivation> staleStarting = proxyActivationRepository
                .findByStatusAndActivatedAtBefore(ProxyActivation.STATUS_STARTING, cutoff);
        for (ProxyActivation activation : staleStarting) {
            try {
                String state = ec2ProxyManager.getInstanceState(activation.getInstanceId());
                if ("running".equals(state)) {
                    activation.setStatus(ProxyActivation.STATUS_ACTIVE);
                    proxyActivationRepository.save(activation);
                    logger.info("Reconcile: Proxy #" + activation.getId() + " promoted to ACTIVE (EC2 running)");
                } else {
                    activation.setStatus(ProxyActivation.STATUS_FAILED);
                    activation.setStoppedAt(LocalDateTime.now());
                    proxyActivationRepository.save(activation);
                    refundCredits(activation.getUser(), activation.getCreditsSpent(), activation.getProxyId());
                    logger.warning("Reconcile: Proxy #" + activation.getId() + " marked FAILED — credits refunded");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Reconcile: Failed to handle stale STARTING #" + activation.getId(), e);
            }
        }

        // 3. Check for leaked instances (EC2 running but no ACTIVE/STARTING DB record)
        for (Map.Entry<String, String> entry : proxyProperties.getInstances().entrySet()) {
            String proxyId = entry.getKey();
            String instanceId = entry.getValue();
            try {
                Optional<ProxyActivation> active = proxyActivationRepository.findByProxyIdAndStatusIn(
                        proxyId, List.of(ProxyActivation.STATUS_ACTIVE, ProxyActivation.STATUS_STARTING));
                if (active.isEmpty()) {
                    String state = ec2ProxyManager.getInstanceState(instanceId);
                    if ("running".equals(state) || "pending".equals(state)) {
                        logger.warning("Reconcile: EC2 instance " + instanceId + " (" + proxyId
                                + ") is running with no active DB record — stopping it");
                        ec2ProxyManager.stopInstance(instanceId);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Reconcile: Failed to check leaked instance " + instanceId, e);
            }
        }

        // 4. Process any overdue expirations right now
        processExpiredActivations();

        logger.info("Proxy reconciliation complete");
    }

    // ==================== HELPERS ====================

    private void refundCredits(User user, int amount, String proxyId) {
        try {
            List<Character> characters = characterService.getAllUserCharacters(user);
            if (characters.isEmpty()) {
                logger.warning("Cannot refund " + amount + " ₳ to user " + user.getUsername() + " — no characters found");
                return;
            }
            // Refund to the top character (by kills, first in list)
            Character topChar = characters.getFirst();
            int currentPoints = topChar.getCurrencyPoints() != null ? topChar.getCurrencyPoints() : 0;
            topChar.setCurrencyPoints(currentPoints + amount);
            characterService.saveAll(List.of(topChar));

            transactionLogService.logTransaction(
                    user, topChar, "PROXY_REFUND",
                    "Proxy refund: " + proxyId,
                    "proxy_refund_" + proxyId,
                    amount, topChar.getCurrencyPoints());

            logger.info("Refunded " + amount + " ₳ to " + topChar.getPlayerName() + " for failed proxy " + proxyId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to refund " + amount + " ₳ to user " + user.getUsername(), e);
        }
    }
}
