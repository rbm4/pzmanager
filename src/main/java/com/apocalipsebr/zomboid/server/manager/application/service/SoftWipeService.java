package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SoftWipe;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SoftWipeStatus;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.SoftWipeRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.TransactionLogRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class SoftWipeService {

    private static final Logger logger = Logger.getLogger(SoftWipeService.class.getName());

    /**
     * Bin tile size — must match MapCleanerService.BIN_TILE_SIZE (8).
     */
    private static final int BIN_TILE_SIZE = 8;

    /**
     * Minimum selection size in tiles to prevent tiny/accidental wipes.
     */
    private static final int MIN_SELECTION_SIZE = 10;

    private final SoftWipeRepository softWipeRepository;
    private final CharacterService characterService;
    private final MapCleanerService mapCleanerService;
    private final TransactionLogService transactionLogService;
    private final TransactionLogRepository transactionLogRepository;
    private final DiscordNotificationService discordNotificationService;

    @Value("${softwipe.area-cost-factor:0.0002}")
    private double areaCostFactor;

    @Value("${softwipe.min-cost:50}")
    private int minCost;

    @Value("${softwipe.enabled:true}")
    private boolean softWipeEnabled;

    public SoftWipeService(SoftWipeRepository softWipeRepository,
                           CharacterService characterService,
                           MapCleanerService mapCleanerService,
                           TransactionLogService transactionLogService,
                           TransactionLogRepository transactionLogRepository,
                           DiscordNotificationService discordNotificationService) {
        this.softWipeRepository = softWipeRepository;
        this.characterService = characterService;
        this.mapCleanerService = mapCleanerService;
        this.transactionLogService = transactionLogService;
        this.transactionLogRepository = transactionLogRepository;
        this.discordNotificationService = discordNotificationService;
    }

    // ==================== RECORDS ====================

    public record CreateSoftWipeResult(boolean success, String message, SoftWipe wipe) {}
    public record CancelResult(boolean success, String message) {}
    public record SoftWipePreview(int totalBins, int cost, int area) {}

    // ==================== COST CALCULATION ====================

    /**
     * Calculates the cost of a soft-wipe for the given rectangle.
     * Cost = max(minCost, ceil(area * areaCostFactor))
     */
    public int calculateCost(int x1, int y1, int x2, int y2) {
        int area = Math.abs(x2 - x1) * Math.abs(y2 - y1);
        int cost = (int) Math.ceil(area * areaCostFactor);
        return Math.max(minCost, cost);
    }

    /**
     * Returns a preview of the soft-wipe for the given rectangle.
     * Includes total bin count and cost. The actual protected bins are computed
     * at execution time (during restart) so the latest data is used.
     */
    public SoftWipePreview getPreview(int x1, int y1, int x2, int y2) {
        int nx1 = Math.min(x1, x2), ny1 = Math.min(y1, y2);
        int nx2 = Math.max(x1, x2), ny2 = Math.max(y1, y2);

        int area = (nx2 - nx1) * (ny2 - ny1);
        int totalBins = countBinsInRect(nx1, ny1, nx2, ny2);
        int cost = calculateCost(nx1, ny1, nx2, ny2);

        return new SoftWipePreview(totalBins, cost, area);
    }

    // ==================== REQUEST SOFT WIPE ====================

    /**
     * Creates a new soft-wipe request. Validates coordinates, checks the user
     * has sufficient currency, deducts points (draining characters one-by-one),
     * and persists the SoftWipe entity in WAITING_RESTART status.
     */
    @Transactional
    public CreateSoftWipeResult requestSoftWipe(User user, int x1, int y1, int x2, int y2) {
        if (!softWipeEnabled) {
            return new CreateSoftWipeResult(false, "Soft-wipe está desabilitado no momento.", null);
        }

        // Normalize coordinates
        int nx1 = Math.min(x1, x2), ny1 = Math.min(y1, y2);
        int nx2 = Math.max(x1, x2), ny2 = Math.max(y1, y2);

        // Validate minimum size
        int dx = nx2 - nx1;
        int dy = ny2 - ny1;
        if (dx < MIN_SELECTION_SIZE || dy < MIN_SELECTION_SIZE) {
            return new CreateSoftWipeResult(false,
                    "Área selecionada muito pequena. Tamanho mínimo: " + MIN_SELECTION_SIZE + " tiles.", null);
        }

        // Calculate cost
        int cost = calculateCost(nx1, ny1, nx2, ny2);

        // Check user balance
        List<Character> userCharacters = characterService.getAllUserCharacters(user);
        int totalCurrency = userCharacters.stream()
                .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                .sum();

        if (totalCurrency < cost) {
            return new CreateSoftWipeResult(false,
                    "Saldo insuficiente. Custo: " + cost + " ₳, Saldo: " + totalCurrency + " ₳", null);
        }

        // Create the entity first so we have an ID for transaction logs
        SoftWipe softWipe = new SoftWipe(user, nx1, ny1, nx2, ny2, cost);
        softWipe = softWipeRepository.save(softWipe);

        // Deduct currency from characters (same drain pattern as GameEventService)
        int remainingCost = cost;
        for (Character character : userCharacters) {
            if (remainingCost <= 0) break;
            int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
            if (currentPoints > 0) {
                int deduction = Math.min(currentPoints, remainingCost);
                character.setCurrencyPoints(currentPoints - deduction);
                remainingCost -= deduction;

                transactionLogService.logTransaction(
                        user, character, "SOFT_WIPE",
                        "Soft-Wipe: (" + nx1 + "," + ny1 + ") → (" + nx2 + "," + ny2 + ")",
                        "softwipe_" + softWipe.getId(),
                        deduction, character.getCurrencyPoints());

                logger.info("Deducted " + deduction + " ₳ from character: " + character.getPlayerName()
                        + " for soft-wipe #" + softWipe.getId());
            }
        }
        characterService.saveAll(userCharacters);

        logger.info("Soft-wipe #" + softWipe.getId() + " created by " + user.getUsername()
                + " — area (" + nx1 + "," + ny1 + ") → (" + nx2 + "," + ny2 + "), cost: " + cost + " ₳");

        return new CreateSoftWipeResult(true,
                "Soft-wipe solicitado com sucesso! Custo: " + cost + " ₳. Será executado no próximo restart.",
                softWipe);
    }

    // ==================== RESTART INTEGRATION ====================

    /**
     * Transitions all WAITING_RESTART wipes to WIPE_AT_RESTART.
     * Called from ServerRestartService.initiateRestart() when a restart begins.
     * This ensures the wipes are marked for execution even if the JVM is killed
     * before the restart script runs.
     */
    @Transactional
    public void markWipesForRestart() {
        int count = softWipeRepository.updateStatusBatch(
                SoftWipeStatus.WAITING_RESTART, SoftWipeStatus.WIPE_AT_RESTART);
        if (count > 0) {
            logger.info("Marked " + count + " soft-wipe(s) for execution at restart");
        }
    }

    /**
     * Executes all pending WIPE_AT_RESTART soft-wipes.
     * Called on ApplicationReadyEvent (after boot) to process wipes queued
     * from the previous restart cycle.
     *
     * For each wipe: converts the rectangle to bin keys, calls
     * MapCleanerService.deleteBins(), and updates the entity with results.
     */
    @Transactional
    public void executeAllPendingWipes() {
        if (!softWipeEnabled) {
            logger.info("Soft-wipe execution skipped at boot because the feature is disabled");
            return;
        }
        List<SoftWipe> pendingWipes = softWipeRepository.findByStatus(SoftWipeStatus.WIPE_AT_RESTART);

        if (pendingWipes.isEmpty()) {
            logger.info("No pending soft-wipes to execute at boot");
            return;
        }

        logger.info("Executing " + pendingWipes.size() + " pending soft-wipe(s) at boot");

        int totalDeleted = 0;
        int totalProtected = 0;

        for (SoftWipe wipe : pendingWipes) {
            try {
                executeSingleWipe(wipe);
                totalDeleted += wipe.getBinsDeleted();
                totalProtected += wipe.getBinsProtected();
            } catch (Exception e) {
                logger.severe("Failed to execute soft-wipe #" + wipe.getId() + ": " + e.getMessage());
                wipe.setStatus(SoftWipeStatus.FAILED);
                wipe.setExecutedAt(LocalDateTime.now());
                wipe.setErrorMessage(e.getMessage());
                softWipeRepository.save(wipe);
            }
        }

        logger.info("Soft-wipe execution complete: " + pendingWipes.size() + " wipe(s), "
                + totalDeleted + " bins deleted, " + totalProtected + " bins protected");

        // Send Discord notification
        try {
            discordNotificationService.sendSoftWipesExecuted(pendingWipes.size(), totalDeleted, totalProtected);
        } catch (Exception e) {
            logger.warning("Failed to send soft-wipe Discord notification: " + e.getMessage());
        }
    }

    /**
     * Executes a single soft-wipe: converts rectangle to bin keys, calls
     * MapCleanerService, and updates the entity.
     */
    private void executeSingleWipe(SoftWipe wipe) {
        logger.info("Executing soft-wipe #" + wipe.getId() + " — area ("
                + wipe.getX1() + "," + wipe.getY1() + ") → (" + wipe.getX2() + "," + wipe.getY2() + ")");

        // Convert rectangle (tile coords) to bin keys
        List<String> binKeys = rectangleToBinKeys(wipe.getX1(), wipe.getY1(), wipe.getX2(), wipe.getY2());

        if (binKeys.isEmpty()) {
            wipe.setStatus(SoftWipeStatus.COMPLETED);
            wipe.setExecutedAt(LocalDateTime.now());
            wipe.setBinsDeleted(0);
            wipe.setBinsProtected(0);
            softWipeRepository.save(wipe);
            logger.info("Soft-wipe #" + wipe.getId() + " completed — no bins in area");
            return;
        }

        // Call MapCleanerService to perform the actual deletion (with safehouse + car protections)
        MapCleanerService.DeleteResult result = mapCleanerService.deleteBins(binKeys);

        wipe.setStatus(result.success() ? SoftWipeStatus.COMPLETED : SoftWipeStatus.FAILED);
        wipe.setExecutedAt(LocalDateTime.now());
        wipe.setBinsDeleted(result.deletedCount());
        wipe.setBinsProtected(result.protectedCount() + result.carProtectedCount());
        if (!result.success()) {
            wipe.setErrorMessage(result.message());
        }

        softWipeRepository.save(wipe);

        logger.info("Soft-wipe #" + wipe.getId() + " " + (result.success() ? "completed" : "failed")
                + " — deleted " + result.deletedCount() + ", protected " + (result.protectedCount() + result.carProtectedCount())
                + " out of " + binKeys.size() + " total bins");
    }

    // ==================== CANCEL ====================

    /**
     * Cancels a soft-wipe request that is still in WAITING_RESTART status.
     * Refunds the currency to the user's characters via cashback.
     */
    @Transactional
    public CancelResult cancelSoftWipe(Long wipeId, User user) {
        Optional<SoftWipe> opt = softWipeRepository.findById(wipeId);
        if (opt.isEmpty()) {
            return new CancelResult(false, "Soft-wipe não encontrado.");
        }

        SoftWipe wipe = opt.get();

        // Verify ownership
        if (!wipe.getUser().getId().equals(user.getId())) {
            return new CancelResult(false, "Você não tem permissão para cancelar este soft-wipe.");
        }

        // Only WAITING_RESTART can be cancelled
        if (wipe.getStatus() != SoftWipeStatus.WAITING_RESTART) {
            return new CancelResult(false,
                    "Não é possível cancelar um soft-wipe no status: " + wipe.getStatus().getDisplayName());
        }

        // Cashback: find transaction logs for this soft-wipe and refund
        var transactions = transactionLogRepository.findByItemIdRefAndCashbackFalse("softwipe_" + wipe.getId());
        for (var txLog : transactions) {
            try {
                transactionLogService.cashback(txLog.getId(), "system_softwipe_cancel");
            } catch (Exception e) {
                logger.warning("Failed to cashback transaction #" + txLog.getId() + ": " + e.getMessage());
            }
        }

        wipe.setStatus(SoftWipeStatus.CANCELLED);
        softWipeRepository.save(wipe);

        logger.info("Soft-wipe #" + wipe.getId() + " cancelled by " + user.getUsername()
                + " — " + wipe.getCost() + " ₳ refunded");

        return new CancelResult(true, "Soft-wipe cancelado e " + wipe.getCost() + " ₳ devolvidos.");
    }

    // ==================== QUERIES ====================

    public List<SoftWipe> getUserWipes(User user) {
        return softWipeRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public boolean isSoftWipeEnabled() {
        return softWipeEnabled;
    }

    public double getAreaCostFactor() {
        return areaCostFactor;
    }

    public int getMinCost() {
        return minCost;
    }

    // ==================== HELPERS ====================

    /**
     * Converts a tile-coordinate rectangle to a list of "bx/by" bin keys.
     */
    private List<String> rectangleToBinKeys(int x1, int y1, int x2, int y2) {
        int bx1 = Math.floorDiv(Math.min(x1, x2), BIN_TILE_SIZE);
        int by1 = Math.floorDiv(Math.min(y1, y2), BIN_TILE_SIZE);
        int bx2 = ceilDiv(Math.max(x1, x2), BIN_TILE_SIZE);
        int by2 = ceilDiv(Math.max(y1, y2), BIN_TILE_SIZE);

        List<String> keys = new ArrayList<>();
        for (int bx = bx1; bx <= bx2; bx++) {
            for (int by = by1; by <= by2; by++) {
                keys.add(bx + "/" + by);
            }
        }
        return keys;
    }

    /**
     * Counts the number of bins that fall within the given tile-coordinate rectangle.
     */
    private int countBinsInRect(int x1, int y1, int x2, int y2) {
        int bx1 = Math.floorDiv(x1, BIN_TILE_SIZE);
        int by1 = Math.floorDiv(y1, BIN_TILE_SIZE);
        int bx2 = ceilDiv(x2, BIN_TILE_SIZE);
        int by2 = ceilDiv(y2, BIN_TILE_SIZE);
        return (bx2 - bx1 + 1) * (by2 - by1 + 1);
    }

    /** Integer ceiling division (works correctly for negative dividends). */
    private static int ceilDiv(int a, int b) {
        return Math.floorDiv(a + b - 1, b);
    }
}
