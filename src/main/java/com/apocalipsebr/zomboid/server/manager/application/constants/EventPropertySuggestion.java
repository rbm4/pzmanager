package com.apocalipsebr.zomboid.server.manager.application.constants;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting;

import java.util.Arrays;
import java.util.List;

/**
 * Curated list of property suggestions available for user-created events.
 * This enum is the single source of truth for all event property validation.
 * Users see only displayName and description — never raw property keys.
 *
 * <p>
 * For PERCENTAGE types, users choose from predefined tiers:
 * 5%, 10%, 15%, 30%, 50%, 100%.
 * For BOOLEAN types, the value is always "true" (toggles on).
 * For ABSOLUTE types, users enter a value within [minValue, maxValue].
 * </p>
 */
public enum EventPropertySuggestion {

    // ==================== SANDBOX PROPERTIES (Global Server Changes) ====================

    // XP_BOOST(
    //     "Aumento de XP", "MultiplierConfig.Global", PropertyTarget.SANDBOX,
    //     SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
    //     "Aumenta a taxa de experiência de todos os jogadores",
    //     1.0, 0.05, 3.0, 100
    // ),

    // ---- LOOT SETTINGS (individual categories) ----

    FOOD_LOOT_NEW(
        "Loot de Comida", "FoodLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Comida perecível e alimentos que podem estragar",
        0.6, 0.0, 4.0, 170
    ),

    LITERATURE_LOOT_NEW(
        "Loot de Literatura", "LiteratureLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Itens de leitura, incluindo panfletos e livros",
        0.6, 0.0, 4.0, 270
    ),

    MEDICAL_LOOT_NEW(
        "Loot Médico", "MedicalLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Remédios, bandagens e itens de primeiros socorros",
        0.6, 0.0, 4.0, 170
    ),

    SURVIVAL_GEARS_LOOT_NEW(
        "Loot de Sobrevivência", "SurvivalGearsLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Varas de pesca, barracas e equipamento de camping",
        0.6, 0.0, 4.0, 170
    ),

    CANNED_FOOD_LOOT_NEW(
        "Loot de Enlatados", "CannedFoodLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Comida enlatada, seca e bebidas",
        0.6, 0.0, 4.0, 270
    ),

    WEAPON_LOOT_NEW(
        "Loot de Armas", "WeaponLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Armas que não são ferramentas de outras categorias",
        0.6, 0.0, 4.0, 390
    ),

    RANGED_WEAPON_LOOT_NEW(
        "Loot de Armas de Fogo", "RangedWeaponLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Inclui acessórios e anexos para armas de fogo",
        0.6, 0.0, 4.0, 450
    ),

    AMMO_LOOT_NEW(
        "Loot de Munição", "AmmoLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Munição solta, caixas e carregadores",
        0.6, 0.0, 4.0, 650
    ),

    MECHANICS_LOOT_NEW(
        "Loot de Mecânica", "MechanicsLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Peças de veículos e ferramentas para instalação",
        0.6, 0.0, 4.0, 180
    ),

    OTHER_LOOT_NEW(
        "Loot Diversos", "OtherLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Todo o resto. Afeta também forrageamento em zonas urbanas",
        0.6, 0.0, 4.0, 170
    ),

    CLOTHING_LOOT_NEW(
        "Loot de Roupas", "ClothingLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Itens vestíveis que não são contêineres",
        0.6, 0.0, 4.0, 170
    ),

    CONTAINER_LOOT_NEW(
        "Loot de Mochilas", "ContainerLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Mochilas e contêineres vestíveis/equipáveis",
        0.6, 0.0, 4.0, 200
    ),

    KEY_LOOT_NEW(
        "Loot de Chaves", "KeyLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Chaves de prédios/carros, chaveiros e cadeados",
        0.6, 0.0, 4.0, 170
    ),

    MEDIA_LOOT_NEW(
        "Loot de Mídia", "MediaLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Fitas VHS e CDs",
        0.6, 0.0, 4.0, 250
    ),

    MEMENTO_LOOT_NEW(
        "Loot de Lembranças", "MementoLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Itens Spiffo, pelúcias e colecionáveis",
        0.6, 0.0, 4.0, 50
    ),

    COOKWARE_LOOT_NEW(
        "Loot de Utensílios", "CookwareLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Itens usados na culinária, incluindo facas. Não inclui comida",
        0.6, 0.0, 4.0, 165
    ),

    MATERIAL_LOOT_NEW(
        "Loot de Materiais", "MaterialLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Ingredientes para fabricação ou construção. Não inclui ferramentas",
        0.6, 0.0, 4.0, 165
    ),

    FARMING_LOOT_NEW(
        "Loot de Fazenda", "FarmingLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Itens de agricultura como sementes, pás e espátulas",
        0.6, 0.0, 4.0, 165
    ),

    TOOL_LOOT_NEW(
        "Loot de Ferramentas", "ToolLootNew", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Ferramentas diversas não incluídas em outras categorias",
        0.6, 0.0, 4.0, 180
    ),

    FARMING_SPEED(
        "Velocidade de Cultivo", "Farming", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Acelera o crescimento de plantações e cultivos",
        1.0, 0.05, 3.0, 180
    ),

    MELEE_XP_BOOST(
        "XP de Combate Corpo-a-Corpo", "XPMultiplier.Maintenance", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Aumenta a experiência ganha em combate corpo-a-corpo",
        1.0, 0.05, 3.0, 490
    ),

    CRAFTING_XP_BOOST(
        "XP de Fabricação", "XPMultiplier.Crafting", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Aumenta a experiência ao fabricar itens",
        1.0, 0.05, 3.0, 570
    ),

    COOKING_XP_BOOST(
        "XP de Culinária", "XPMultiplier.Cooking", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Aumenta a experiência ao cozinhar",
        1.0, 0.05, 3.0, 360
    ),

    NATURE_ABUNDANCE(
        "Abundância Natural", "NatureAbundance", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Aumenta recursos naturais como frutas, bagas e cogumelos",
        1.0, 0.05, 3.0, 190
    ),

    FITNESS_XP_BOOST(
        "XP de Condicionamento", "XPMultiplier.Fitness", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Aumenta a experiência de condicionamento físico",
        1.0, 0.05, 3.0, 260
    ),

    SURVIVAL_XP_BOOST(
        "XP de Sobrevivência", "XPMultiplier.Survival", PropertyTarget.SANDBOX,
        SandboxSetting.ConfigType.SANDBOX_VARS, ValueType.PERCENTAGE,
        "Aumenta a experiência de habilidades de sobrevivência",
        1.0, 0.05, 3.0, 130
    ),

    // ==================== REGION PROPERTIES (Zone-specific Changes) ====================

    SPRINTER_ZONE(
        "Zona de Sprinters", "sprinterChance", PropertyTarget.REGION,
        null, ValueType.PERCENTAGE,
        "Cria uma zona perigosa com zumbis corredores",
        0.0, 5.0, 100.0, 500
    ),

    PVP_ZONE(
        "Zona de Combate PVP", "pvpEnabled", PropertyTarget.REGION,
        null, ValueType.BOOLEAN,
        "Habilita combate entre jogadores na zona definida",
        null, null, null, 500
    ),

    TOUGH_ZOMBIE_ZONE(
        "Zona de Zumbis Resistentes", "toughnessChance", PropertyTarget.REGION,
        null, ValueType.PERCENTAGE,
        "Cria uma zona com zumbis mais difíceis de matar",
        0.0, 5.0, 100.0, 350
    ),

    ARMORED_ZOMBIE_ZONE(
        "Zona de Zumbis Blindados", "zombieArmorFactor", PropertyTarget.REGION,
        null, ValueType.PERCENTAGE,
        "Cria uma zona onde zumbis possuem blindagem extra",
        0.0, 5.0, 100.0, 530
    ),

    HAWK_VISION_ZONE(
        "Zona de Visão Aguçada", "hawkVisionChance", PropertyTarget.REGION,
        null, ValueType.PERCENTAGE,
        "Cria uma zona onde zumbis detectam jogadores com facilidade",
        0.0, 5.0, 100.0, 320
    ),

    GOOD_HEARING_ZONE(
        "Zona de Audição Apurada", "goodHearingChance", PropertyTarget.REGION,
        null, ValueType.PERCENTAGE,
        "Cria uma zona onde zumbis possuem audição ampliada",
        0.0, 5.0, 100.0, 310
    ),

    SUPERHUMAN_ZOMBIE_ZONE(
        "Zona de Zumbis Super-Humanos", "superhumanChance", PropertyTarget.REGION,
        null, ValueType.PERCENTAGE,
        "Cria uma zona com zumbis extremamente poderosos",
        0.0, 5.0, 100.0, 350
    ),

    REGION_MESSAGE(
        "Mensagem da Região", "regionMessage", PropertyTarget.REGION,
        null, ValueType.TEXT,
        "Exibe uma mensagem personalizada quando jogadores entram na zona",
        null, null, null, 550
    ),

    KILL_POINTS_MULTIPLIER(
        "Multiplicador de Pontos por Kill", "killPointsMultiplier", PropertyTarget.REGION,
        null, ValueType.ABSOLUTE,
        "Aumenta os pontos ganhos por abater zumbis dentro da zona",
        1.0, 5.0, 100.0, 150
    );

    // ==================== PERCENTAGE TIERS ====================

    /** Available percentage options for PERCENTAGE value types. */
    public static final int[] PERCENTAGE_TIERS = {5, 10, 15, 30, 50, 100};

    /** Cost multiplier for each percentage tier (index-matched with PERCENTAGE_TIERS). */
    public static final int[] PERCENTAGE_COST_MULTIPLIERS = {1, 2, 3, 6, 10, 20};

    // ==================== NESTED ENUMS ====================

    public enum PropertyTarget {
        SANDBOX,
        REGION
    }

    public enum ValueType {
        PERCENTAGE,
        ABSOLUTE,
        BOOLEAN,
        TEXT
    }

    // ==================== FIELDS ====================

    private final String displayName;
    private final String propertyKey;
    private final PropertyTarget target;
    private final SandboxSetting.ConfigType configType;
    private final ValueType valueType;
    private final String description;
    private final Double baseValue;
    private final Double minValue;
    private final Double maxValue;
    private final int baseCost;

    EventPropertySuggestion(String displayName, String propertyKey, PropertyTarget target,
                            SandboxSetting.ConfigType configType, ValueType valueType,
                            String description, Double baseValue, Double minValue,
                            Double maxValue, int baseCost) {
        this.displayName = displayName;
        this.propertyKey = propertyKey;
        this.target = target;
        this.configType = configType;
        this.valueType = valueType;
        this.description = description;
        this.baseValue = baseValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.baseCost = baseCost;
    }

    // ==================== COST CALCULATION ====================

    /**
     * Calculates the cost for a given percentage tier.
     *
     * @throws IllegalArgumentException if the tier is not in PERCENTAGE_TIERS
     */
    public int calculateCost(int percentageTier) {
        if (valueType == ValueType.BOOLEAN || valueType == ValueType.TEXT) {
            return baseCost;
        }
        for (int i = 0; i < PERCENTAGE_TIERS.length; i++) {
            if (PERCENTAGE_TIERS[i] == percentageTier) {
                return baseCost * PERCENTAGE_COST_MULTIPLIERS[i];
            }
        }
        throw new IllegalArgumentException("Invalid percentage tier: " + percentageTier);
    }

    /**
     * Calculates the cost for an absolute value selection.
     */
    public int calculateCostForAbsolute(double value) {
        if (valueType != ValueType.ABSOLUTE) {
            throw new IllegalStateException("Not an absolute value property");
        }
        if (maxValue == null || maxValue == 0) return baseCost;
        double ratio = Math.abs(value - (baseValue != null ? baseValue : 0)) / maxValue;
        return (int) Math.ceil(baseCost * Math.max(1, ratio * 20));
    }

    /**
     * Calculates the delta value for a percentage tier.
     * <ul>
     *   <li>SANDBOX PERCENTAGE: fraction of base value (e.g., 20% of 1.0 = 0.2)</li>
     *   <li>REGION PERCENTAGE: the percentage itself is the value (e.g., 30 = 30% chance)</li>
     *   <li>BOOLEAN: always 1.0 (true)</li>
     * </ul>
     */
    public double calculateDelta(int percentageTier) {
        if (valueType == ValueType.BOOLEAN || valueType == ValueType.TEXT) {
            return 1.0;
        }
        if (target == PropertyTarget.SANDBOX) {
            return (baseValue != null ? baseValue : 1.0) * percentageTier / 100.0;
        } else {
            return percentageTier;
        }
    }

    /**
     * Returns the string representation of the calculated delta.
     * For BOOLEAN: "true". For numbers: the formatted decimal.
     */
    public String calculateDeltaString(int percentageTier) {
        if (valueType == ValueType.BOOLEAN || valueType == ValueType.TEXT) {
            return "true";
        }
        double delta = calculateDelta(percentageTier);
        if (delta == Math.floor(delta)) {
            if (delta >= Integer.MIN_VALUE && delta <= Integer.MAX_VALUE) {
                return String.valueOf((int) delta);
            } else {
                return String.valueOf(delta);
            }
        }
        return String.valueOf(delta);
    }

    /** Validates whether a given percentage tier is allowed. */
    public boolean isValidPercentageTier(int tier) {
        for (int t : PERCENTAGE_TIERS) {
            if (t == tier) return true;
        }
        return false;
    }

    /** Validates whether a given absolute value is within allowed bounds. */
    public boolean isValidAbsoluteValue(double value) {
        if (minValue != null && value < minValue) return false;
        if (maxValue != null && value > maxValue) return false;
        return true;
    }

    // ==================== STATIC HELPERS ====================

    public static List<EventPropertySuggestion> getSandboxSuggestions() {
        return Arrays.stream(values())
            .filter(s -> s.target == PropertyTarget.SANDBOX)
            .toList();
    }

    public static List<EventPropertySuggestion> getRegionSuggestions() {
        return Arrays.stream(values())
            .filter(s -> s.target == PropertyTarget.REGION)
            .toList();
    }

    /**
     * Safely resolves an enum value by name. Returns null if not found.
     */
    public static EventPropertySuggestion fromName(String name) {
        if (name == null) return null;
        try {
            return valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ==================== GETTERS ====================

    public String getDisplayName() { return displayName; }
    public String getPropertyKey() { return propertyKey; }
    public PropertyTarget getTarget() { return target; }
    public SandboxSetting.ConfigType getConfigType() { return configType; }
    public ValueType getValueType() { return valueType; }
    public String getDescription() { return description; }
    public Double getBaseValue() { return baseValue; }
    public Double getMinValue() { return minValue; }
    public Double getMaxValue() { return maxValue; }
    public int getBaseCost() { return baseCost; }
}
