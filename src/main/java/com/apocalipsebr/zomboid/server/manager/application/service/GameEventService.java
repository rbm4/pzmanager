package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.application.constants.EventPropertySuggestion;
import com.apocalipsebr.zomboid.server.manager.application.constants.EventPropertySuggestion.PropertyTarget;
import com.apocalipsebr.zomboid.server.manager.application.constants.EventPropertySuggestion.ValueType;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.*;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEvent.EventStatus;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class GameEventService {

    private static final Logger logger = Logger.getLogger(GameEventService.class.getName());

    private final GameEventRepository gameEventRepository;
    private final GameEventPropertyRepository gameEventPropertyRepository;
    private final GameEventContributionRepository gameEventContributionRepository;
    private final SandboxSettingRepository sandboxSettingRepository;
    private final RegionRepository regionRepository;
    private final RegionCustomPropertyRepository regionCustomPropertyRepository;
    private final CharacterService characterService;
    private final TransactionLogService transactionLogService;
    private final TransactionLogRepository transactionLogRepository;

    public GameEventService(GameEventRepository gameEventRepository,
                            GameEventPropertyRepository gameEventPropertyRepository,
                            GameEventContributionRepository gameEventContributionRepository,
                            SandboxSettingRepository sandboxSettingRepository,
                            RegionRepository regionRepository,
                            RegionCustomPropertyRepository regionCustomPropertyRepository,
                            CharacterService characterService,
                            TransactionLogService transactionLogService,
                            TransactionLogRepository transactionLogRepository) {
        this.gameEventRepository = gameEventRepository;
        this.gameEventPropertyRepository = gameEventPropertyRepository;
        this.gameEventContributionRepository = gameEventContributionRepository;
        this.sandboxSettingRepository = sandboxSettingRepository;
        this.regionRepository = regionRepository;
        this.regionCustomPropertyRepository = regionCustomPropertyRepository;
        this.characterService = characterService;
        this.transactionLogService = transactionLogService;
        this.transactionLogRepository = transactionLogRepository;
    }

    // ==================== QUERY METHODS ====================

    public Page<GameEvent> getEventsPaginated(String search, EventStatus status, Pageable pageable) {
        if (search == null && status == null) {
            return gameEventRepository.findAllOrdered(pageable);
        }
        return gameEventRepository.searchEvents(search, status, pageable);
    }

    public Optional<GameEvent> getEventById(Long id) {
        return gameEventRepository.findById(id);
    }

    public long countByStatus(EventStatus status) {
        return gameEventRepository.countByStatus(status);
    }

    public long getTotalEventCount() {
        return gameEventRepository.count();
    }

    /**
     * Returns all properties from currently active events, grouped by property key.
     * Before collecting, each active event is checked for expiration — if its expiration date
     * has passed, the event is immediately deactivated and excluded from results.
     *
     * @param propertyTarget optional filter (e.g. "SANDBOX" or "REGION"); if null, returns all properties
     * @return a map of propertyKey → list of GameEventProperty from non-expired active events
     */
    @Transactional
    public Map<String, List<GameEventProperty>> getAllActiveEventsProperties(String propertyTarget) {
        LocalDateTime now = LocalDateTime.now();
        List<GameEvent> activeEvents = gameEventRepository.findByStatus(EventStatus.ACTIVE);
        List<GameEvent> validEvents = new ArrayList<>();

        for (GameEvent event : activeEvents) {
            if (event.getExpirationDate() != null && event.getExpirationDate().isBefore(now)) {
                try {
                    deactivateEvent(event);
                    logger.info("getAllActiveEventsProperties: auto-expired event '" + event.getTitle() + "' (ID: " + event.getId() + ")");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to auto-expire event during property retrieval: " + event.getId(), e);
                }
            } else {
                validEvents.add(event);
            }
        }

        return validEvents.stream()
                .flatMap(event -> event.getProperties().stream())
                .filter(prop -> propertyTarget == null || propertyTarget.equals(prop.getPropertyTarget()))
                .collect(java.util.stream.Collectors.groupingBy(GameEventProperty::getPropertyKey));
    }

    /** Maximum events a user can create per week. */
    private static final int WEEKLY_EVENT_LIMIT = 3;

    /**
     * Returns how many events the user created in the current week (last 7 days).
     */
    public long getWeeklyEventsCreated(User user) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        return gameEventRepository.countByCreatedByAndCreatedAtAfter(user, oneWeekAgo);
    }

    /**
     * Returns how many events the user can still create this week.
     */
    public int getWeeklyEventsRemaining(User user) {
        long created = getWeeklyEventsCreated(user);
        return Math.max(0, WEEKLY_EVENT_LIMIT - (int) created);
    }

    // ==================== CREATE EVENT ====================

    public record CreateEventResult(boolean success, String message, GameEvent event) {}

    /**
     * Creates a new event from validated user selections.
     * All property keys and values are validated against the EventPropertySuggestion enum.
     * Costs are recalculated server-side, never trusting client values.
     *
     * @param title          Event title
     * @param description    Event description
     * @param user           The creating user
     * @param suggestionKeys List of EventPropertySuggestion enum names selected
     * @param selectedValues List of selected values (percentage tier or "true")
     * @param regionX1       Zone X1 coordinate (for region properties, nullable)
     * @param regionX2       Zone X2 coordinate
     * @param regionY1       Zone Y1 coordinate
     * @param regionY2       Zone Y2 coordinate
     * @param regionZ        Zone Z coordinate
     * @param regionName     User-defined region name (for region properties, nullable)
     */
    @Transactional
    public CreateEventResult createEvent(String title, String description, User user,
                                         List<String> suggestionKeys, List<String> selectedValues,
                                         Integer regionX1, Integer regionX2,
                                         Integer regionY1, Integer regionY2, Integer regionZ,
                                         String regionName) {
        if (title == null || title.trim().isEmpty()) {
            return new CreateEventResult(false, "Título do evento é obrigatório", null);
        }
        if (suggestionKeys == null || suggestionKeys.isEmpty()) {
            return new CreateEventResult(false, "Selecione pelo menos uma propriedade", null);
        }
        if (suggestionKeys.size() != selectedValues.size()) {
            return new CreateEventResult(false, "Dados inválidos", null);
        }

        // Weekly creation limit
        if (getWeeklyEventsRemaining(user) <= 0) {
            return new CreateEventResult(false,
                "Você atingiu o limite de " + WEEKLY_EVENT_LIMIT + " eventos por semana. Tente novamente mais tarde.", null);
        }

        GameEvent event = new GameEvent();
        event.setTitle(title.trim());
        event.setDescription(description != null ? description.trim() : null);
        event.setCreatedBy(user);
        event.setStatus(EventStatus.PENDING);
        event.setDurationDays(7);

        int totalCost = 0;
        boolean hasRegionProps = false;

        for (int i = 0; i < suggestionKeys.size(); i++) {
            String key = suggestionKeys.get(i);
            String value = selectedValues.get(i);

            // Validate against enum — never trust user input
            EventPropertySuggestion suggestion = EventPropertySuggestion.fromName(key);
            if (suggestion == null) {
                return new CreateEventResult(false, "Propriedade inválida: " + key, null);
            }

            // Validate value
            int percentageTier = 0;
            if (suggestion.getValueType() == ValueType.BOOLEAN) {
                value = "true";
            } else if (suggestion.getValueType() == ValueType.TEXT) {
                // Text type — value is the user-provided text
                if (value == null || value.trim().isEmpty()) {
                    return new CreateEventResult(false, "Texto obrigatório para " + suggestion.getDisplayName(), null);
                }
                value = value.trim();
            } else if (suggestion.getValueType() == ValueType.PERCENTAGE) {
                try {
                    percentageTier = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return new CreateEventResult(false, "Valor inválido para " + suggestion.getDisplayName(), null);
                }
                if (!suggestion.isValidPercentageTier(percentageTier)) {
                    return new CreateEventResult(false, "Porcentagem inválida para " + suggestion.getDisplayName(), null);
                }
            } else if (suggestion.getValueType() == ValueType.ABSOLUTE) {
                try {
                    double absValue = Double.parseDouble(value);
                    if (!suggestion.isValidAbsoluteValue(absValue)) {
                        return new CreateEventResult(false, "Valor fora do limite para " + suggestion.getDisplayName(), null);
                    }
                } catch (NumberFormatException e) {
                    return new CreateEventResult(false, "Valor inválido para " + suggestion.getDisplayName(), null);
                }
            }

            // Calculate cost server-side
            int cost;
            if (suggestion.getValueType() == ValueType.BOOLEAN || suggestion.getValueType() == ValueType.TEXT) {
                cost = suggestion.calculateCost(0);
            } else if (suggestion.getValueType() == ValueType.PERCENTAGE) {
                cost = suggestion.calculateCost(percentageTier);
            } else {
                cost = suggestion.calculateCostForAbsolute(Double.parseDouble(value));
            }

            // Calculate delta server-side
            String delta;
            if (suggestion.getValueType() == ValueType.BOOLEAN) {
                delta = "true";
            } else if (suggestion.getValueType() == ValueType.TEXT) {
                delta = value; // Text value IS the delta
            } else if (suggestion.getValueType() == ValueType.PERCENTAGE) {
                delta = suggestion.calculateDeltaString(percentageTier);
            } else {
                delta = value;
            }

            // Validate maxValue for SANDBOX PERCENTAGE properties
            if (suggestion.getTarget() == PropertyTarget.SANDBOX
                && suggestion.getValueType() == ValueType.PERCENTAGE
                && suggestion.getMaxValue() != null) {
                try {
                    double currentValue = getCurrentSandboxValue(suggestion);
                    double deltaNum = parseDouble(delta, 0);
                    if (currentValue + deltaNum > suggestion.getMaxValue()) {
                        return new CreateEventResult(false,
                            suggestion.getDisplayName() + " excederia o valor máximo permitido ("
                            + suggestion.getMaxValue() + "). Valor atual: " + currentValue, null);
                    }
                } catch (IllegalStateException e) {
                    return new CreateEventResult(false,
                        "Configuração de sandbox não encontrada para: " + suggestion.getDisplayName(), null);
                }
            }

            GameEventProperty prop = new GameEventProperty();
            prop.setSuggestionKey(suggestion.name());
            prop.setPropertyTarget(suggestion.getTarget().name());
            prop.setPropertyKey(suggestion.getPropertyKey());
            prop.setDisplayName(suggestion.getDisplayName());
            prop.setValueType(suggestion.getValueType().name());
            prop.setSelectedValue(value);
            prop.setCalculatedDelta(delta);
            prop.setPropertyCost(cost);

            if (suggestion.getTarget() == PropertyTarget.REGION) {
                hasRegionProps = true;
                prop.setRegionX1(regionX1);
                prop.setRegionX2(regionX2);
                prop.setRegionY1(regionY1);
                prop.setRegionY2(regionY2);
                prop.setRegionZ(regionZ != null ? regionZ : 0);
            }

            event.addProperty(prop);
            totalCost += cost;
        }

        // Validate region coordinates if any region properties were selected
        if (hasRegionProps) {
            if (regionX1 == null || regionX2 == null || regionY1 == null || regionY2 == null) {
                return new CreateEventResult(false, "Coordenadas da zona são obrigatórias para propriedades de região", null);
            }

            // Check for overlap with existing enabled regions
            String overlapCheck = checkRegionOverlap(regionX1, regionX2, regionY1, regionY2);
            if (overlapCheck != null) {
                return new CreateEventResult(false, overlapCheck, null);
            }
        }

        event.setTotalCost(totalCost);

        // Store region name if any region properties were selected
        if (hasRegionProps && regionName != null && !regionName.trim().isEmpty()) {
            event.setRegionName(regionName.trim());
        }

        GameEvent saved = gameEventRepository.save(event);

        logger.info("Event created: " + saved.getTitle() + " (ID: " + saved.getId()
                + ", Cost: " + totalCost + " ₳) by user " + user.getUsername());

        return new CreateEventResult(true, "Evento criado com sucesso! Custo total: " + totalCost + " ₳", saved);
    }

    // ==================== CONTRIBUTE ====================

    public record ContributeResult(boolean success, String message, boolean activated) {}

    /**
     * Allows a user to contribute currency towards a pending event.
     * Deducts currency from the user's characters in order, capped by available balance and remaining amount.
     * If the contribution makes the event fully funded, it is automatically activated.
     */
    @Transactional
    public ContributeResult contribute(Long eventId, Integer requestedAmount, User user) {
        GameEvent event = gameEventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento não encontrado"));

        if (event.getStatus() != EventStatus.PENDING) {
            return new ContributeResult(false, "Este evento não está aceitando contribuições", false);
        }

        if (requestedAmount == null || requestedAmount <= 0) {
            return new ContributeResult(false, "Valor inválido", false);
        }

        // Get user's characters and total balance
        List<Character> userCharacters = characterService.getUserCharacters(user);
        int totalCurrency = userCharacters.stream()
            .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
            .sum();

        // Cap contribution
        int remaining = event.getRemainingAmount();
        int effectiveAmount = Math.min(requestedAmount, remaining);
        effectiveAmount = Math.min(effectiveAmount, totalCurrency);

        if (effectiveAmount <= 0) {
            return new ContributeResult(false, "Saldo insuficiente para contribuir", false);
        }

        // Deduct currency from characters (same logic as ZomboidItemService)
        int remainingCost = effectiveAmount;
        for (Character character : userCharacters) {
            if (remainingCost <= 0) break;
            int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
            if (currentPoints > 0) {
                int deduction = Math.min(currentPoints, remainingCost);
                character.setCurrencyPoints(currentPoints - deduction);
                remainingCost -= deduction;

                // Log transaction for each character deduction
                transactionLogService.logTransaction(
                    user, character, "EVENT_CONTRIBUTION",
                    "Evento: " + event.getTitle(), "event_" + event.getId(),
                    deduction, character.getCurrencyPoints());

                logger.info("Deducted " + deduction + " ₳ from character: " + character.getPlayerName()
                        + " for event " + event.getTitle());
            }
        }
        characterService.saveAll(userCharacters);

        // Record contribution
        GameEventContribution contribution = new GameEventContribution(user, effectiveAmount);
        event.addContribution(contribution);
        event.setAmountCollected(event.getAmountCollected() + effectiveAmount);

        // Check if fully funded → activate
        if (event.isFullyFunded()) {
            activateEvent(event);
            return new ContributeResult(true,
                "Contribuição de " + effectiveAmount + " ₳ registrada! Evento financiado e ativado!",
                true);
        }

        gameEventRepository.save(event);
        return new ContributeResult(true,
            "Contribuição de " + effectiveAmount + " ₳ registrada! Faltam " + event.getRemainingAmount() + " ₳",
            false);
    }

    // ==================== ACTIVATION ====================

    /**
     * Activates a fully funded event. For sandbox properties, updates the SandboxSetting
     * appliedValue so the overwrite takes effect at next server restart.
     * For region properties, creates Region entities with the event's zone and properties.
     */
    @Transactional
    public void activateEvent(GameEvent event) {
        logger.info("Activating event: " + event.getTitle() + " (ID: " + event.getId() + ")");

        // Activate sandbox properties
        for (GameEventProperty prop : event.getProperties()) {
            if ("SANDBOX".equals(prop.getPropertyTarget())) {
                activateSandboxProperty(prop);
            }
        }

        // Activate region properties (grouped into a single region)
        activateRegionProperties(event);

        event.setStatus(EventStatus.ACTIVE);
        event.setActivatedAt(LocalDateTime.now());
        event.setExpirationDate(LocalDateTime.now().plusDays(event.getDurationDays()));
        gameEventRepository.save(event);

        logger.info("Event activated successfully: " + event.getTitle());
    }

    private void activateSandboxProperty(GameEventProperty prop) {
        EventPropertySuggestion suggestion = prop.getSuggestion();
        if (suggestion == null || suggestion.getConfigType() == null) {
            logger.warning("Cannot activate sandbox property — invalid suggestion: " + prop.getSuggestionKey());
            return;
        }

        try {
            Optional<SandboxSetting> settingOpt = sandboxSettingRepository
                .findBySettingKeyAndConfigType(suggestion.getPropertyKey(), suggestion.getConfigType());

            if (settingOpt.isPresent()) {
                SandboxSetting setting = settingOpt.get();
                String effectiveValue = setting.getEffectiveValue();
                double currentVal = parseDouble(effectiveValue, suggestion.getBaseValue() != null ? suggestion.getBaseValue() : 0);
                double delta = parseDouble(prop.getCalculatedDelta(), 0);
                double newVal = Math.ceil((currentVal + delta) * 100) / 100;

                setting.setAppliedValue(String.valueOf(newVal));
                setting.setOverwriteAtStartup(true);
                setting.setUpdatedAt(LocalDateTime.now());
                sandboxSettingRepository.save(setting);

                logger.info("Activated sandbox property: " + suggestion.getPropertyKey()
                        + " (" + currentVal + " + " + delta + " = " + newVal + ")");
            } else {
                logger.warning("SandboxSetting not found for key: " + suggestion.getPropertyKey()
                        + " (configType: " + suggestion.getConfigType() + ")");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to activate sandbox property: " + prop.getPropertyKey(), e);
        }
    }

    private void activateRegionProperties(GameEvent event) {
        List<GameEventProperty> regionProps = event.getProperties().stream()
            .filter(p -> "REGION".equals(p.getPropertyTarget()))
            .toList();

        if (regionProps.isEmpty()) return;

        GameEventProperty first = regionProps.get(0);
        String regionCode = "event_" + event.getId();
        String regionName = (event.getRegionName() != null && !event.getRegionName().isEmpty())
            ? event.getRegionName()
            : "Evento: " + event.getTitle();

        Region region = new Region();
        region.setCode(regionCode);
        region.setName(regionName);
        region.setCategories("CUSTOM");
        region.setX1(first.getRegionX1());
        region.setX2(first.getRegionX2());
        region.setY1(first.getRegionY1());
        region.setY2(first.getRegionY2());
        region.setZ(first.getRegionZ() != null ? first.getRegionZ() : 0);
        region.setEnabled(true);
        region.setPermanent(false);
        region.setExpirationDate(event.getExpirationDate() != null
            ? event.getExpirationDate().toLocalDate()
            : LocalDate.now().plusDays(event.getDurationDays()));

        for (GameEventProperty prop : regionProps) {
            RegionCustomProperty customProp = new RegionCustomProperty(
                prop.getPropertyKey(),
                prop.getCalculatedDelta()
            );
            region.addCustomProperty(customProp);
        }

        Region saved = regionRepository.save(region);

        // Link all region properties to the created region
        for (GameEventProperty prop : regionProps) {
            prop.setLinkedRegionId(saved.getId());
        }

        logger.info("Created event region: " + regionCode + " with " + regionProps.size() + " properties");
    }

    // ==================== DEACTIVATION ====================

    /**
     * Deactivates an active event. Reverts sandbox property changes and disables region zones.
     */
    @Transactional
    public void deactivateEvent(GameEvent event) {
        logger.info("Deactivating event: " + event.getTitle() + " (ID: " + event.getId() + ")");

        // Revert sandbox properties
        for (GameEventProperty prop : event.getProperties()) {
            if ("SANDBOX".equals(prop.getPropertyTarget())) {
                deactivateSandboxProperty(prop);
            }
        }

        // Disable event regions
        deactivateRegionProperties(event);

        event.setStatus(EventStatus.EXPIRED);
        event.setExpiredAt(LocalDateTime.now());
        gameEventRepository.save(event);

        logger.info("Event deactivated: " + event.getTitle());
    }

    private void deactivateSandboxProperty(GameEventProperty prop) {
        EventPropertySuggestion suggestion = prop.getSuggestion();
        if (suggestion == null || suggestion.getConfigType() == null) return;

        try {
            Optional<SandboxSetting> settingOpt = sandboxSettingRepository
                .findBySettingKeyAndConfigType(suggestion.getPropertyKey(), suggestion.getConfigType());

            if (settingOpt.isPresent()) {
                SandboxSetting setting = settingOpt.get();
                String effectiveValue = setting.getEffectiveValue();
                double currentVal = parseDouble(effectiveValue, suggestion.getBaseValue() != null ? suggestion.getBaseValue() : 0);
                double delta = parseDouble(prop.getCalculatedDelta(), 0);
                double newVal = currentVal - delta;

                setting.setAppliedValue(String.valueOf(newVal));
                setting.setUpdatedAt(LocalDateTime.now());
                sandboxSettingRepository.save(setting);

                logger.info("Reverted sandbox property: " + suggestion.getPropertyKey()
                        + " (" + currentVal + " - " + delta + " = " + newVal + ")");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to revert sandbox property: " + prop.getPropertyKey(), e);
        }
    }

    private void deactivateRegionProperties(GameEvent event) {
        List<GameEventProperty> regionProps = event.getProperties().stream()
            .filter(p -> "REGION".equals(p.getPropertyTarget()) && p.getLinkedRegionId() != null)
            .toList();

        Set<Long> processedRegionIds = new HashSet<>();
        for (GameEventProperty prop : regionProps) {
            if (processedRegionIds.add(prop.getLinkedRegionId())) {
                regionRepository.findById(prop.getLinkedRegionId()).ifPresent(region -> {
                    region.setEnabled(false);
                    regionRepository.save(region);
                    logger.info("Disabled event region: " + region.getCode());
                });
            }
        }
    }

    // ==================== SCHEDULED EXPIRATION CHECK ====================

    /**
     * Periodically checks and expires active events past their expiration date,
     * and cancels pending events older than their allowed funding window.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processExpiredEvents() {
        LocalDateTime now = LocalDateTime.now();

        // Expire active events
        List<GameEvent> activeEvents = gameEventRepository.findByStatus(EventStatus.ACTIVE);
        for (GameEvent event : activeEvents) {
            if (event.getExpirationDate() != null && event.getExpirationDate().isBefore(now)) {
                try {
                    deactivateEvent(event);
                    logger.info("Auto-expired active event: " + event.getTitle());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to auto-expire event: " + event.getId(), e);
                }
            }
        }

        // Cancel unfunded pending events past their deadline
        List<GameEvent> pendingEvents = gameEventRepository.findByStatus(EventStatus.PENDING);
        for (GameEvent event : pendingEvents) {
            LocalDateTime deadline = event.getCreatedAt().plusDays(event.getDurationDays());
            if (deadline.isBefore(now)) {
                try {
                    cashbackEventContributions(event);
                    event.setStatus(EventStatus.CANCELLED);
                    event.setExpiredAt(now);
                    gameEventRepository.save(event);
                    logger.info("Auto-cancelled unfunded event: " + event.getTitle() + " — contributions refunded");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to cancel/cashback event: " + event.getId(), e);
                }
            }
        }
    }

    /**
     * Refunds all contributions for a cancelled/expired unfunded event.
     * Finds TransactionLog entries with itemIdRef="event_{id}" and type="EVENT_CONTRIBUTION",
     * then cashbacks each one to return currency to the contributing characters.
     */
    @Transactional
    public void cashbackEventContributions(GameEvent event) {
        String eventRef = "event_" + event.getId();
        List<TransactionLog> eventTransactions = transactionLogRepository
            .findByItemIdRefAndTransactionTypeAndCashbackFalse(eventRef, "EVENT_CONTRIBUTION");

        for (TransactionLog log : eventTransactions) {
            try {
                transactionLogService.cashback(log.getId(), "SYSTEM_EVENT_EXPIRY");
                logger.info("Cashback applied for event contribution: transaction #" + log.getId()
                        + " (" + log.getAmount() + " ₳ to " + log.getCharacterName() + ")");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to cashback transaction #" + log.getId(), e);
            }
        }
    }

    // ==================== CANCEL EVENT (by creator) ====================

    public record CancelEventResult(boolean success, String message) {}

    /**
     * Allows the event creator to cancel their own PENDING event.
     * All contributions are refunded via cashback.
     */
    @Transactional
    public CancelEventResult cancelEvent(Long eventId, User user) {
        GameEvent event = gameEventRepository.findById(eventId)
            .orElse(null);

        if (event == null) {
            return new CancelEventResult(false, "Evento não encontrado");
        }

        if (!event.getCreatedBy().getId().equals(user.getId())) {
            return new CancelEventResult(false, "Apenas o criador do evento pode cancelá-lo");
        }

        if (event.getStatus() != EventStatus.PENDING) {
            return new CancelEventResult(false, "Apenas eventos pendentes podem ser cancelados");
        }

        // Cashback all contributions
        cashbackEventContributions(event);

        event.setStatus(EventStatus.CANCELLED);
        event.setExpiredAt(LocalDateTime.now());
        gameEventRepository.save(event);

        logger.info("Event cancelled by creator: " + event.getTitle()
                + " (ID: " + event.getId() + ") by " + user.getUsername());

        return new CancelEventResult(true, "Evento cancelado com sucesso. Todas as contribuições foram reembolsadas.");
    }

    // ==================== REGION OVERLAP CHECK ====================

    /**
     * Checks if the proposed zone coordinates overlap with any existing enabled region.
     *
     * @return Error message if overlap detected, null if clear
     */
    public String checkRegionOverlap(int x1, int x2, int y1, int y2) {
        // Normalize coordinates
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);

        List<Region> enabledRegions = regionRepository.findByEnabledTrue();
        for (Region existing : enabledRegions) {
            int eMinX = Math.min(existing.getX1(), existing.getX2());
            int eMaxX = Math.max(existing.getX1(), existing.getX2());
            int eMinY = Math.min(existing.getY1(), existing.getY2());
            int eMaxY = Math.max(existing.getY1(), existing.getY2());

            // AABB overlap test
            if (minX < eMaxX && maxX > eMinX && minY < eMaxY && maxY > eMinY) {
                return "A zona proposta sobrepõe a região existente: " + existing.getName()
                        + " (" + existing.getCode() + ")";
            }
        }
        return null;
    }

    // ==================== HELPERS ====================

    /**
     * Calculates the total currency balance for a user across all characters.
     */
    public int getUserBalance(User user) {
        return characterService.getUserCharacters(user).stream()
            .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
            .sum();
    }

    // ==================== SANDBOX VALUE FILTERING ====================

    /**
     * Result record holding filtered sandbox suggestions and a map of disabled tiers.
     * Suggestions that have NO valid tier at all are excluded from the list entirely.
     *
     * @param suggestions     Only suggestions that have at least one valid tier
     * @param disabledTiersMap Map of suggestion enum name → comma-separated disabled tier values (e.g. "50,100")
     */
    public record FilteredSandboxSuggestions(
        List<EventPropertySuggestion> suggestions,
        Map<String, String> disabledTiersMap
    ) {}

    /**
     * Filters sandbox suggestions by checking current SandboxSetting effective values.
     * For each PERCENTAGE suggestion with a maxValue, calculates which tiers would exceed the max.
     * <ul>
     *   <li>If ALL tiers would exceed → suggestion is discarded entirely.</li>
     *   <li>If SOME tiers would exceed → suggestion is included but those tiers are in disabledTiersMap.</li>
     *   <li>If SandboxSetting entity not found → suggestion is discarded and a warning is logged.</li>
     * </ul>
     */
    public FilteredSandboxSuggestions getFilteredSandboxSuggestions() {
        List<EventPropertySuggestion> all = EventPropertySuggestion.getSandboxSuggestions();
        List<EventPropertySuggestion> available = new ArrayList<>();
        Map<String, String> disabledMap = new HashMap<>();

        for (EventPropertySuggestion s : all) {
            // Non-percentage types don't need maxValue filtering
            if (s.getValueType() != ValueType.PERCENTAGE || s.getMaxValue() == null) {
                available.add(s);
                continue;
            }

            double currentValue;
            try {
                currentValue = getCurrentSandboxValue(s);
            } catch (IllegalStateException e) {
                logger.warning("Excluding suggestion " + s.name() + ": " + e.getMessage());
                continue; // Skip — SandboxSetting not found
            }

            // Determine which tiers are valid
            List<String> disabledTiers = new ArrayList<>();
            boolean anyValid = false;

            for (int tier : EventPropertySuggestion.PERCENTAGE_TIERS) {
                double delta = s.calculateDelta(tier);
                if (currentValue + delta > s.getMaxValue()) {
                    disabledTiers.add(String.valueOf(tier));
                } else {
                    anyValid = true;
                }
            }

            if (!anyValid) {
                logger.info("Suggestion " + s.name() + " fully disabled — current value "
                        + currentValue + " already near max " + s.getMaxValue());
                continue; // All tiers exceed — discard entirely
            }

            available.add(s);
            if (!disabledTiers.isEmpty()) {
                disabledMap.put(s.name(), String.join(",", disabledTiers));
            }
        }

        return new FilteredSandboxSuggestions(available, disabledMap);
    }

    /**
     * Looks up the current effective value for a SANDBOX suggestion from the SandboxSetting table.
     *
     * @throws IllegalStateException if no SandboxSetting entity is found for the property key
     */
    private double getCurrentSandboxValue(EventPropertySuggestion suggestion) {
        if (suggestion.getConfigType() == null) {
            return suggestion.getBaseValue() != null ? suggestion.getBaseValue() : 0;
        }

        Optional<SandboxSetting> setting = sandboxSettingRepository
            .findBySettingKeyAndConfigType(suggestion.getPropertyKey(), suggestion.getConfigType());

        if (setting.isEmpty()) {
            throw new IllegalStateException(
                "Configuração de sandbox não encontrada para: " + suggestion.getPropertyKey()
                + " (configType: " + suggestion.getConfigType() + ")");
        }

        return parseDouble(setting.get().getEffectiveValue(),
            suggestion.getBaseValue() != null ? suggestion.getBaseValue() : 0);
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
