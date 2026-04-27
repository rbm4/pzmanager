package com.apocalipsebr.zomboid.server.manager.application.service;

import static com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.BIN_TILE_SIZE;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apocalipsebr.zomboid.server.manager.application.service.MapDataService.SafehouseInfo;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SafehouseClaimRequest;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SafehouseClaimStatus;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.SafehouseClaimRequestRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.TransactionLogRepository;

@Service
public class SafehouseService {

    private static final Logger log = LoggerFactory.getLogger(SafehouseService.class);
    private static final int DEFAULT_MARGIN = 2;
    private static final int MIN_SELECTION_SIZE = 10;
    private static final int HARD_MIN_CLAIM_COST = 6000;
    private static final int HARD_MIN_UPGRADE_COST = 1500;
    /**
     * Z-levels to include for each chunk: -1 (basement) through +4 (upper floors).
     */
    private static final int Z_MIN = -1;
    private static final int Z_MAX = 4;

    private final MapDataService mapDataService;
    private final CharacterService characterService;
    private final ServerCommandService serverCommandService;
    private final TransactionLogService transactionLogService;
    private final TransactionLogRepository transactionLogRepository;
    private final SafehouseClaimRequestRepository safehouseClaimRequestRepository;
    private final EmailService emailService;

    @Value("${safehouse.claim.area-cost-factor:0.31}")
    private double areaCostFactor;

    @Value("${safehouse.claim.min-cost:6000}")
    private int minCost;

    @Value("${safehouse.claim.free-area:1600}")
    private int freeArea;

    @Value("${safehouse.upgrade.min-cost:1500}")
    private int upgradeMinCost;

    @Value("${safehouse.upgrade.free-area:400}")
    private int upgradeFreeArea;

    public SafehouseService(MapDataService mapDataService,
            CharacterService characterService,
            ServerCommandService serverCommandService,
            TransactionLogService transactionLogService,
            TransactionLogRepository transactionLogRepository,
            SafehouseClaimRequestRepository safehouseClaimRequestRepository,
            EmailService emailService) {
        this.mapDataService = mapDataService;
        this.characterService = characterService;
        this.serverCommandService = serverCommandService;
        this.transactionLogService = transactionLogService;
        this.transactionLogRepository = transactionLogRepository;
        this.safehouseClaimRequestRepository = safehouseClaimRequestRepository;
        this.emailService = emailService;
    }

    public SafehouseListResult listSafehouses() {
        List<String> warnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(warnings);
        return new SafehouseListResult(safehouses, warnings);
    }

    public ClaimPreview previewClaim(int x1, int y1, int x2, int y2) {
        NormalizedRect rect = normalizeRect(x1, y1, x2, y2);
        List<SafehouseInfo> overlaps = findOverlappingSafehouses(rect);
        int cost = calculateClaimCost(rect.area());
        return new ClaimPreview(
                cost,
                cost,
                rect.area(),
                !overlaps.isEmpty(),
                overlaps.size(),
                overlaps.stream()
                        .map(sh -> sh.name() != null && !sh.name().isBlank() ? sh.name() : sh.owner())
                        .filter(Objects::nonNull)
                        .toList());
    }

    public List<SafehouseClaimRequest> getUserClaims(User user) {
        return safehouseClaimRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<SafehouseClaimRequest> getPendingClaims() {
        return safehouseClaimRequestRepository.findByStatusOrderByCreatedAtAsc(SafehouseClaimStatus.PENDING_REVIEW);
    }

    public List<SafehouseClaimRequest> getRecentClaims() {
        return safehouseClaimRequestRepository.findAll().stream()
                .sorted(Comparator.comparing(SafehouseClaimRequest::getCreatedAt).reversed())
                .toList();
    }

    @Transactional
    public CreateClaimResult requestClaim(User user, Long ownerCharacterId, int x1, int y1, int x2, int y2) {
        if (ownerCharacterId == null) {
            return new CreateClaimResult(false, "Selecione um personagem para ser dono da safehouse.", null);
        }

        List<Character> seasonCharacters = characterService.getUserCharacters(user);
        Character ownerCharacter = seasonCharacters.stream()
                .filter(c -> Objects.equals(c.getId(), ownerCharacterId))
                .findFirst()
                .orElse(null);
        if (ownerCharacter == null) {
            return new CreateClaimResult(false, "Personagem inválido para esta conta.", null);
        }

        String ownerCharacterName = ownerCharacter.getPlayerName() != null ? ownerCharacter.getPlayerName().trim() : "";
        if (ownerCharacterName.isBlank()) {
            return new CreateClaimResult(false, "O personagem selecionado não possui nome válido.", null);
        }
        if (ownerCharacterName.contains("##")) {
            return new CreateClaimResult(false, "Nome do personagem inválido para envio ao servidor.", null);
        }

        NormalizedRect rect = normalizeRect(x1, y1, x2, y2);
        if (rect.width() < MIN_SELECTION_SIZE || rect.height() < MIN_SELECTION_SIZE) {
            return new CreateClaimResult(false,
                    "Área selecionada muito pequena. Tamanho mínimo: " + MIN_SELECTION_SIZE + " tiles.", null);
        }

        List<SafehouseInfo> overlaps = findOverlappingSafehouses(rect);
        if (!overlaps.isEmpty()) {
            return new CreateClaimResult(false,
                    "A area selecionada sobrepoe uma safehouse existente. Escolha outra regiao.", null);
        }

        int cost = calculateClaimCost(rect.area());

        List<Character> userCharacters = characterService.getAllUserCharacters(user);
        int totalCurrency = userCharacters.stream()
                .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                .sum();
        if (totalCurrency < cost) {
            return new CreateClaimResult(false,
                    "Saldo insuficiente. Custo: " + cost + " ₳, Saldo: " + totalCurrency + " ₳", null);
        }

        SafehouseClaimRequest claim = new SafehouseClaimRequest(
                user,
                ownerCharacterName,
                rect.x1(),
                rect.y1(),
                rect.x2(),
                rect.y2(),
                cost,
                !overlaps.isEmpty(),
                overlaps.size());
        claim = safehouseClaimRequestRepository.save(claim);

        int remainingCost = cost;
        for (Character character : userCharacters) {
            if (remainingCost <= 0) {
                break;
            }
            int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
            if (currentPoints <= 0) {
                continue;
            }

            int deduction = Math.min(currentPoints, remainingCost);
            character.setCurrencyPoints(currentPoints - deduction);
            remainingCost -= deduction;

            transactionLogService.logTransaction(
                    user,
                    character,
                    "SAFEHOUSE_CLAIM",
                    "Safehouse Claim (Owner: " + claim.getClaimName() + ")",
                    claimTransactionRef(claim.getId()),
                    deduction,
                    character.getCurrencyPoints());
        }
        characterService.saveAll(userCharacters);
        emailService.sendTextEmail("ricardo.malafaia1994@gmail.com", "Nova solicitação de safehouse",
                "Uma nova solicitação de safehouse foi feita para o personagem: " + ownerCharacterName);

        return new CreateClaimResult(
                true,
                "Solicitacao enviada para revisao administrativa. " + cost + " ₳ foram reservados em escrow.",
                claim);
    }

    public ClaimPreview previewUpgrade(User user,
            int originalX,
            int originalY,
            int originalW,
            int originalH,
            int x1,
            int y1,
            int x2,
            int y2) {
        SafehouseInfo targetSafehouse = findUpgradeableSafehouse(user, originalX, originalY, originalW, originalH);
        NormalizedRect rect = normalizeRect(x1, y1, x2, y2);
        UpgradeValidation validation = validateUpgradeSelection(targetSafehouse, rect);
        int cost = calculateUpgradeCost(validation.addedArea());
        return new ClaimPreview(cost, cost, validation.addedArea(), validation.overlapsExisting(),
                validation.overlapCount(), validation.overlappingSafehouses());
    }

    @Transactional
    public CreateClaimResult requestUpgrade(User user,
            int originalX,
            int originalY,
            int originalW,
            int originalH,
            int x1,
            int y1,
            int x2,
            int y2) {
        SafehouseInfo targetSafehouse;
        try {
            targetSafehouse = findUpgradeableSafehouse(user, originalX, originalY, originalW, originalH);
        } catch (IllegalArgumentException e) {
            return new CreateClaimResult(false, e.getMessage(), null);
        }

        String ownerCharacterName = targetSafehouse.owner() != null ? targetSafehouse.owner().trim() : "";
        if (ownerCharacterName.isBlank()) {
            return new CreateClaimResult(false, "A safehouse alvo nao possui dono valido.", null);
        }
        if (ownerCharacterName.contains("##")) {
            return new CreateClaimResult(false, "Nome do personagem invalido para envio ao servidor.", null);
        }

        NormalizedRect rect = normalizeRect(x1, y1, x2, y2);
        UpgradeValidation validation;
        try {
            validation = validateUpgradeSelection(targetSafehouse, rect);
        } catch (IllegalArgumentException e) {
            return new CreateClaimResult(false, e.getMessage(), null);
        }

        int cost = calculateUpgradeCost(validation.addedArea());

        List<Character> userCharacters = characterService.getAllUserCharacters(user);
        int totalCurrency = userCharacters.stream()
                .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                .sum();
        if (totalCurrency < cost) {
            return new CreateClaimResult(false,
                    "Saldo insuficiente. Custo: " + cost + " ₳, Saldo: " + totalCurrency + " ₳", null);
        }

        SafehouseClaimRequest claim = new SafehouseClaimRequest(
                user,
                ownerCharacterName,
                rect.x1(),
                rect.y1(),
                rect.x2(),
                rect.y2(),
                cost,
                validation.overlapsExisting(),
                validation.overlapCount());
        claim.setClaimType("UPGRADE");
        claim = safehouseClaimRequestRepository.save(claim);

        int remainingCost = cost;
        for (Character character : userCharacters) {
            if (remainingCost <= 0)
                break;
            int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
            if (currentPoints <= 0)
                continue;
            int deduction = Math.min(currentPoints, remainingCost);
            character.setCurrencyPoints(currentPoints - deduction);
            remainingCost -= deduction;
            transactionLogService.logTransaction(
                    user, character,
                    "SAFEHOUSE_UPGRADE",
                    "Safehouse Upgrade (Owner: " + claim.getClaimName() + ")",
                    claimTransactionRef(claim.getId()),
                    deduction,
                    character.getCurrencyPoints());
        }
        characterService.saveAll(userCharacters);
        emailService.sendTextEmail("ricardo.malafaia1994@gmail.com", "Nova solicitação de upgrade de safehouse",
                "Uma nova solicitação de upgrade de safehouse foi feita para o personagem: " + ownerCharacterName);

        return new CreateClaimResult(
                true,
                "Solicitacao de upgrade enviada para revisao administrativa. " + cost
                        + " ₳ foram reservados em escrow.",
                claim);
    }

    @Transactional
    public ReviewClaimResult approveClaim(Long claimId, String adminUsername, String adminReason) {
        Optional<SafehouseClaimRequest> opt = safehouseClaimRequestRepository.findById(claimId);
        if (opt.isEmpty()) {
            return new ReviewClaimResult(false, "Solicitação não encontrada.", null);
        }

        SafehouseClaimRequest claim = opt.get();
        if (claim.getStatus() != SafehouseClaimStatus.PENDING_REVIEW) {
            return new ReviewClaimResult(false, "A solicitação não está pendente de revisão.", claim);
        }

        int x = Math.min(claim.getX1(), claim.getX2());
        int y = Math.min(claim.getY1(), claim.getY2());
        int w = Math.abs(claim.getX2() - claim.getX1());
        int h = Math.abs(claim.getY2() - claim.getY1());
        if (w <= 0 || h <= 0) {
            return new ReviewClaimResult(false, "Coordenadas inválidas para criar safehouse no servidor.", claim);
        }

        String ownerCharacterName = claim.getClaimName();
        if (ownerCharacterName == null || ownerCharacterName.isBlank() || ownerCharacterName.contains("##")) {
            return new ReviewClaimResult(false, "Nome do personagem dono é inválido para envio ao servidor.", claim);
        }

        String requestId = "claim-" + claim.getId() + "-" + System.currentTimeMillis();
        String commandPrefix = "UPGRADE".equals(claim.getClaimType()) ? "APOCBR_SH_UPGRADE" : "APOCBR_SH";
        String command = "servermsg ##" + commandPrefix + "##" + requestId + "##" + ownerCharacterName +
                "##" + x + "##" + y + "##" + w + "##" + h;

        String playersResponse;
        try {
            playersResponse = serverCommandService.sendCommandResponse("players");
        } catch (RuntimeException e) {
            log.error("Failed to check connected players before approving safehouse claim {}", claimId, e);
            return new ReviewClaimResult(false,
                    "Falha ao verificar jogadores conectados no servidor. A solicitação permanece em escrow/pendente. Erro: "
                            + e.getMessage(),
                    claim);
        }

        int connectedPlayers = extractConnectedPlayersCount(playersResponse);
        if (connectedPlayers < 1) {
            return new ReviewClaimResult(false,
                    "Nao ha jogadores online para retransmitir o comando de safehouse. Tente novamente quando houver ao menos 1 jogador conectado.",
                    claim);
        }

        try {
            serverCommandService.sendCommand(command);
        } catch (RuntimeException e) {
            log.error("Failed to approve safehouse claim {} via RCON command", claimId, e);
            return new ReviewClaimResult(false,
                    "Falha ao enviar comando para o servidor do jogo. A solicitação permanece em escrow/pendente. Erro: "
                            + e.getMessage(),
                    claim);
        }

        claim.setStatus(SafehouseClaimStatus.APPROVED);
        claim.setReviewedBy(adminUsername);
        claim.setReviewedAt(java.time.LocalDateTime.now());
        claim.setAdminReason(adminReason != null && !adminReason.isBlank() ? adminReason.trim() : null);
        safehouseClaimRequestRepository.save(claim);

        return new ReviewClaimResult(true, "Solicitação aprovada.", claim);
    }

    @Transactional
    public ReviewClaimResult denyClaim(Long claimId, String adminUsername, String adminReason) {
        Optional<SafehouseClaimRequest> opt = safehouseClaimRequestRepository.findById(claimId);
        if (opt.isEmpty()) {
            return new ReviewClaimResult(false, "Solicitação não encontrada.", null);
        }

        SafehouseClaimRequest claim = opt.get();
        if (claim.getStatus() != SafehouseClaimStatus.PENDING_REVIEW) {
            return new ReviewClaimResult(false, "A solicitação não está pendente de revisão.", claim);
        }

        for (var txLog : transactionLogRepository.findByItemIdRefAndCashbackFalse(claimTransactionRef(claim.getId()))) {
            transactionLogService.cashback(txLog.getId(), adminUsername);
        }

        claim.setStatus(SafehouseClaimStatus.DENIED);
        claim.setReviewedBy(adminUsername);
        claim.setReviewedAt(java.time.LocalDateTime.now());
        claim.setAdminReason(adminReason != null && !adminReason.isBlank() ? adminReason.trim() : null);
        safehouseClaimRequestRepository.save(claim);

        return new ReviewClaimResult(true, "Solicitação negada e saldo devolvido ao jogador.", claim);
    }

    @Transactional
    public ReviewClaimResult cancelClaim(User user, Long claimId) {
        Optional<SafehouseClaimRequest> opt = safehouseClaimRequestRepository.findById(claimId);
        if (opt.isEmpty()) {
            return new ReviewClaimResult(false, "Solicitação não encontrada.", null);
        }

        SafehouseClaimRequest claim = opt.get();
        if (!Objects.equals(claim.getUser().getId(), user.getId())) {
            return new ReviewClaimResult(false, "Você não pode cancelar esta solicitação.", null);
        }
        if (claim.getStatus() != SafehouseClaimStatus.PENDING_REVIEW) {
            return new ReviewClaimResult(false, "Apenas solicitações pendentes podem ser canceladas.", claim);
        }

        for (var txLog : transactionLogRepository.findByItemIdRefAndCashbackFalse(claimTransactionRef(claim.getId()))) {
            transactionLogService.cashback(txLog.getId(), user.getUsername());
        }

        safehouseClaimRequestRepository.delete(claim);
        return new ReviewClaimResult(true, "Solicitação cancelada e saldo devolvido ao jogador.", null);
    }

    /**
     * Writes a ZIP to the given output stream containing:
     * - map_meta.bin at the root
     * - vehicles.db and vehicles.db-journal at the root (vehicle data)
     * - map/{bx}/{by}.bin (z=0) and map/{bx}/{by}_{z}.bin (z=-1..+4) for each bin
     * around detected safehouses
     *
     * @param marginTiles number of tiles around each safehouse to include (default
     *                    2)
     * @param out         the output stream to write the ZIP to
     * @return metadata about the export
     */
    public ExportResult exportSafehouseBinsAsZip(int marginTiles, OutputStream out) throws IOException {
        if (marginTiles < 0)
            marginTiles = DEFAULT_MARGIN;

        Path mapDir = mapDataService.getMapDir();
        if (mapDir == null) {
            throw new IllegalStateException("Map folder is not configured or does not exist.");
        }

        Path metaPath = mapDataService.getMetaPath();

        List<String> warnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(warnings);

        if (safehouses.isEmpty()) {
            throw new IllegalStateException("No safehouses detected. " + String.join("; ", warnings));
        }

        // Compute the set of bin keys around all safehouses
        Set<String> binKeys = buildSafehouseBinKeys(safehouses, marginTiles);

        log.info("Safehouse export: {} safehouses, {} bin keys (margin={})", safehouses.size(), binKeys.size(),
                marginTiles);

        int binsWritten = 0;
        long totalBytes = 0;
        List<String> missing = new ArrayList<>();

        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            // Include save-root files
            Path saveRoot = mapDir.getParent();
            totalBytes += addRootFileToZip(zos, metaPath, "map_meta.bin", warnings);
            if (saveRoot != null) {
                totalBytes += addRootFileToZip(zos, saveRoot.resolve("vehicles.db"), "vehicles.db", warnings);
                totalBytes += addRootFileToZip(zos, saveRoot.resolve("vehicles.db-journal"), "vehicles.db-journal",
                        warnings);
            }

            // Include bin files for each z-level: map/{bx}/{by}.bin (z=0),
            // map/{bx}/{by}_{z}.bin (z!=0)
            for (String key : binKeys) {
                String[] parts = key.split("/");
                int bx = Integer.parseInt(parts[0]);
                int by = Integer.parseInt(parts[1]);

                for (int z = Z_MIN; z <= Z_MAX; z++) {
                    String fileName = (z == 0) ? by + ".bin" : by + "_" + z + ".bin";
                    Path binFile = mapDir.resolve(String.valueOf(bx)).resolve(fileName);

                    // Security: ensure the path stays within the map folder
                    if (!binFile.normalize().startsWith(mapDir.normalize())) {
                        warnings.add("Path traversal blocked: " + key + " z=" + z);
                        continue;
                    }

                    if (Files.exists(binFile)) {
                        byte[] binData = Files.readAllBytes(binFile);
                        zos.putNextEntry(new ZipEntry("map/" + bx + "/" + fileName));
                        zos.write(binData);
                        zos.closeEntry();
                        binsWritten++;
                        totalBytes += binData.length;
                    } else if (z == 0) {
                        missing.add(key);
                    }
                }
            }

            zos.finish();
        }

        if (!missing.isEmpty()) {
            log.debug("Safehouse export: {} bins not found on disk (expected — not all bins exist)", missing.size());
        }

        log.info("Safehouse export complete: {} bins written, {} bytes total", binsWritten, totalBytes);

        return new ExportResult(safehouses.size(), binKeys.size(), binsWritten, totalBytes, warnings);
    }

    /**
     * Adds a save-root file to the ZIP if it exists, returns bytes written.
     */
    private long addRootFileToZip(ZipOutputStream zos, Path filePath, String zipName, List<String> warnings)
            throws IOException {
        if (filePath != null && Files.exists(filePath)) {
            byte[] data = Files.readAllBytes(filePath);
            zos.putNextEntry(new ZipEntry(zipName));
            zos.write(data);
            zos.closeEntry();
            return data.length;
        }
        warnings.add(zipName + " not found — not included in export.");
        return 0;
    }

    /**
     * Builds a set of "bx/by" bin keys around all safehouses expanded by the given
     * margin.
     */
    private Set<String> buildSafehouseBinKeys(List<SafehouseInfo> safehouses, int marginTiles) {
        Set<String> keys = new LinkedHashSet<>();
        for (SafehouseInfo sh : safehouses) {
            int x1 = Math.floorDiv(sh.x() - marginTiles, BIN_TILE_SIZE);
            int y1 = Math.floorDiv(sh.y() - marginTiles, BIN_TILE_SIZE);
            int x2 = MapDataService.ceilDiv(sh.x() + sh.w() + marginTiles, BIN_TILE_SIZE);
            int y2 = MapDataService.ceilDiv(sh.y() + sh.h() + marginTiles, BIN_TILE_SIZE);

            for (int bx = x1; bx <= x2; bx++) {
                for (int by = y1; by <= y2; by++) {
                    keys.add(bx + "/" + by);
                }
            }
        }
        return keys;
    }

    private int calculateClaimCost(int area) {
        int base = Math.max(minCost, HARD_MIN_CLAIM_COST);
        if (area <= freeArea) {
            return base;
        }
        int extra = (int) Math.ceil((area - freeArea) * areaCostFactor);
        return base + extra;
    }

    private int calculateUpgradeCost(int area) {
        int base = Math.max(upgradeMinCost, HARD_MIN_UPGRADE_COST);
        if (area <= upgradeFreeArea) {
            return base;
        }
        int extra = (int) Math.ceil((area - upgradeFreeArea) * areaCostFactor);
        return base + extra;
    }

    private int extractConnectedPlayersCount(String playersResponse) {
        if (playersResponse == null || playersResponse.isBlank()) {
            return 0;
        }

        String lower = playersResponse.toLowerCase();
        String marker = "players connected (";
        int markerIndex = lower.indexOf(marker);
        if (markerIndex >= 0) {
            int countStart = markerIndex + marker.length();
            int countEnd = lower.indexOf(')', countStart);
            if (countEnd > countStart) {
                String countRaw = playersResponse.substring(countStart, countEnd).trim();
                try {
                    return Integer.parseInt(countRaw);
                } catch (NumberFormatException ignored) {
                    // Fall back to line-based parsing below.
                }
            }
        }

        int fallbackCount = 0;
        for (String line : playersResponse.split("\\R")) {
            if (line != null && line.trim().startsWith("-")) {
                fallbackCount++;
            }
        }
        return fallbackCount;
    }

    private List<SafehouseInfo> findOverlappingSafehouses(NormalizedRect rect) {
        List<String> warnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(warnings);
        return safehouses.stream()
                .filter(sh -> rectanglesOverlap(rect.x1(), rect.y1(), rect.x2(), rect.y2(),
                        sh.x(), sh.y(), sh.x() + sh.w(), sh.y() + sh.h()))
                .toList();
    }

    private SafehouseInfo findUpgradeableSafehouse(User user, int originalX, int originalY, int originalW,
            int originalH) {
        List<Character> userCharacters = characterService.getUserCharacters(user);
        Set<String> ownerNames = new LinkedHashSet<>();
        for (Character character : userCharacters) {
            if (character.getPlayerName() != null && !character.getPlayerName().isBlank()) {
                ownerNames.add(character.getPlayerName().trim());
            }
        }

        List<String> warnings = new ArrayList<>();
        List<SafehouseInfo> safehouses = mapDataService.getSafehouses(warnings);
        return safehouses.stream()
                .filter(sh -> sh.x() == originalX && sh.y() == originalY && sh.w() == originalW && sh.h() == originalH)
                .filter(sh -> sh.owner() != null && ownerNames.contains(sh.owner().trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "A safehouse selecionada para upgrade nao foi encontrada ou nao pertence a esta conta."));
    }

    private UpgradeValidation validateUpgradeSelection(SafehouseInfo targetSafehouse, NormalizedRect rect) {
        if (rect.width() < MIN_SELECTION_SIZE || rect.height() < MIN_SELECTION_SIZE) {
            throw new IllegalArgumentException(
                    "Area selecionada muito pequena. Tamanho minimo: " + MIN_SELECTION_SIZE + " tiles.");
        }

        int targetX1 = targetSafehouse.x();
        int targetY1 = targetSafehouse.y();
        int targetX2 = targetSafehouse.x() + targetSafehouse.w();
        int targetY2 = targetSafehouse.y() + targetSafehouse.h();

        if (rect.x1() > targetX1 || rect.y1() > targetY1 || rect.x2() < targetX2 || rect.y2() < targetY2) {
            throw new IllegalArgumentException(
                    "O upgrade deve cobrir integralmente a safehouse atual para que o servidor identifique a area correta.");
        }

        int targetArea = targetSafehouse.w() * targetSafehouse.h();
        int newArea = rect.area();
        int addedArea = newArea - targetArea;
        if (addedArea <= 0) {
            throw new IllegalArgumentException(
                    "O upgrade precisa aumentar a area da safehouse atual. Selecione uma regiao maior que a original.");
        }

        List<SafehouseInfo> overlaps = findOverlappingSafehouses(rect).stream()
                .filter(sh -> !sameSafehouse(sh, targetSafehouse))
                .toList();
        if (!overlaps.isEmpty()) {
            List<String> names = overlaps.stream()
                    .map(sh -> sh.name() != null && !sh.name().isBlank() ? sh.name() : sh.owner())
                    .filter(Objects::nonNull)
                    .toList();
            throw new IllegalArgumentException(
                    "O upgrade nao pode sobrepor outras safehouses. Ajuste a selecao para manter apenas a safehouse atual dentro da nova area."
                            + (names.isEmpty() ? "" : " Conflitos: " + String.join(", ", names) + "."));
        }

        return new UpgradeValidation(addedArea, false, 0, List.of());
    }

    private boolean sameSafehouse(SafehouseInfo left, SafehouseInfo right) {
        return left.x() == right.x()
                && left.y() == right.y()
                && left.w() == right.w()
                && left.h() == right.h()
                && Objects.equals(left.owner(), right.owner());
    }

    private boolean rectanglesOverlap(int ax1, int ay1, int ax2, int ay2,
            int bx1, int by1, int bx2, int by2) {
        return ax1 < bx2 && ax2 > bx1 && ay1 < by2 && ay2 > by1;
    }

    private NormalizedRect normalizeRect(int x1, int y1, int x2, int y2) {
        int nx1 = Math.min(x1, x2);
        int ny1 = Math.min(y1, y2);
        int nx2 = Math.max(x1, x2);
        int ny2 = Math.max(y1, y2);
        return new NormalizedRect(nx1, ny1, nx2, ny2);
    }

    private String claimTransactionRef(Long claimId) {
        return "safehouse_claim_" + claimId;
    }

    // --- Records ---

    public record SafehouseListResult(List<SafehouseInfo> safehouses, List<String> warnings) {
    }

    public record ClaimPreview(int cost, int baseCost, int area, boolean overlapsExisting,
            int overlapCount, List<String> overlappingSafehouses) {
    }

    public record CreateClaimResult(boolean success, String message, SafehouseClaimRequest claim) {
    }

    public record ReviewClaimResult(boolean success, String message, SafehouseClaimRequest claim) {
    }

    public record ExportResult(int safehouseCount, int totalBinKeys, int binsWritten, long totalBytes,
            List<String> warnings) {
    }

    private record UpgradeValidation(int addedArea, boolean overlapsExisting, int overlapCount,
            List<String> overlappingSafehouses) {
    }

    private record NormalizedRect(int x1, int y1, int x2, int y2) {
        int width() {
            return x2 - x1;
        }

        int height() {
            return y2 - y1;
        }

        int area() {
            return width() * height();
        }
    }
}
