package com.apocalipsebr.zomboid.server.manager.application.constants;

import java.util.Arrays;
import java.util.Optional;

/**
 * Comprehensive enum representing all Project Zomboid Sandbox Variables.
 * Each property includes metadata for reading/writing server configuration files.
 */
public enum SandboxProperty {
    
    // ===== CORE SETTINGS =====
    
    /** File version number */
    VERSION("VERSION", PropertyCategory.CORE, DataType.INTEGER, 6, null, null, "File version number"),
    
    // ===== ZOMBIE SETTINGS =====
    
    /** 
     * Zombie population multiplier. Also sets "Population Multiplier" in Advanced Zombie Options.
     * 1=Insane, 2=Very High, 3=High, 4=Normal, 5=Low, 6=None
     */
    ZOMBIES("Zombies", PropertyCategory.ZOMBIE, DataType.INTEGER, 4, 1, 6, 
            "Zombie population setting. 1=Insane, 2=Very High, 3=High, 4=Normal, 5=Low, 6=None"),
    
    /** 
     * How zombies are distributed across the map.
     * 1=Urban Focused, 2=Uniform
     */
    DISTRIBUTION("Distribution", PropertyCategory.ZOMBIE, DataType.INTEGER, 1, 1, 2,
            "Zombie distribution. 1=Urban Focused, 2=Uniform"),
    
    /** Controls whether randomization is applied to zombie distribution */
    ZOMBIE_VORONOI_NOISE("ZombieVoronoiNoise", PropertyCategory.ZOMBIE, DataType.BOOLEAN, true, null, null,
            "Controls whether some randomization is applied to zombie distribution"),
    
    /** 
     * How frequently new zombies are added to the world.
     * 1=High, 2=Normal, 3=Low, 4=None
     */
    ZOMBIE_RESPAWN("ZombieRespawn", PropertyCategory.ZOMBIE, DataType.INTEGER, 2, 1, 4,
            "How frequently new zombies are added to the world. 1=High, 2=Normal, 3=Low, 4=None"),
    
    /** Zombies allowed to migrate to empty cells */
    ZOMBIE_MIGRATE("ZombieMigrate", PropertyCategory.ZOMBIE, DataType.BOOLEAN, true, null, null,
            "Zombies allowed to migrate to empty cells"),
    
    // ===== TIME & DATE SETTINGS =====
    
    /** 
     * Length of a day in game.
     * 1=15 Minutes, 2=30 Minutes, 3=1 Hour, 4=1 Hour 30 Minutes, 5=2 Hours, etc., 27=Real-time
     */
    DAY_LENGTH("DayLength", PropertyCategory.TIME, DataType.INTEGER, 4, 1, 27,
            "Day length. 4=1 Hour 30 Minutes (default), 27=Real-time"),
    
    /** Starting year (offset from 1993) */
    START_YEAR("StartYear", PropertyCategory.TIME, DataType.INTEGER, 1, 1, 100,
            "Starting year"),
    
    /** 
     * Month in which the game starts.
     * 1=January, 2=February, ..., 7=July (default), ..., 12=December
     */
    START_MONTH("StartMonth", PropertyCategory.TIME, DataType.INTEGER, 7, 1, 12,
            "Month in which the game starts. 1=January, 7=July (default), 12=December"),
    
    /** Day of the month in which the game starts */
    START_DAY("StartDay", PropertyCategory.TIME, DataType.INTEGER, 9, 1, 31,
            "Day of the month in which the games starts"),
    
    /** 
     * Hour of the day in which the game starts.
     * 1=7 AM, 2=9 AM (default), 3=12 PM, 4=2 PM, 5=5 PM, 6=9 PM, 7=12 AM, 8=2 AM, 9=5 AM
     */
    START_TIME("StartTime", PropertyCategory.TIME, DataType.INTEGER, 2, 1, 9,
            "Hour of the day in which the game starts. 2=9 AM (default)"),
    
    /** 
     * Whether the time of day changes naturally.
     * 1=Normal, 2=Endless Day, 3=Endless Night
     */
    DAY_NIGHT_CYCLE("DayNightCycle", PropertyCategory.TIME, DataType.INTEGER, 1, 1, 3,
            "Time of day cycle. 1=Normal, 2=Endless Day, 3=Endless Night"),
    
    /** 
     * How long after apocalypse start to begin (affects erosion/spoilage).
     * 1=0 months, 2=1 month, ..., 13=12 months
     */
    TIME_SINCE_APO("TimeSinceApo", PropertyCategory.TIME, DataType.INTEGER, 1, 1, 13,
            "How long after the end of the world to begin. Affects starting world erosion and food spoilage"),
    
    // ===== WEATHER & CLIMATE =====
    
    /** 
     * Whether weather changes or remains at a single state.
     * 1=Normal, 2=No Weather, 3=Endless Rain, 4=Endless Storm, 5=Endless Snow, 6=Endless Blizzard
     */
    CLIMATE_CYCLE("ClimateCycle", PropertyCategory.WEATHER, DataType.INTEGER, 1, 1, 6,
            "Weather pattern. 1=Normal, 2=No Weather, 3=Endless Rain, 4=Endless Storm, 5=Endless Snow, 6=Endless Blizzard"),
    
    /** 
     * Whether fog occurs naturally.
     * 1=Normal, 2=No Fog, 3=Endless Fog
     */
    FOG_CYCLE("FogCycle", PropertyCategory.WEATHER, DataType.INTEGER, 1, 1, 3,
            "Fog pattern. 1=Normal, 2=No Fog, 3=Endless Fog"),
    
    /** 
     * Global temperature setting.
     * 1=Very Cold, 2=Cold, 3=Normal, 4=Hot, 5=Very Hot
     */
    TEMPERATURE("Temperature", PropertyCategory.WEATHER, DataType.INTEGER, 3, 1, 5,
            "Global temperature. 1=Very Cold, 2=Cold, 3=Normal, 4=Hot, 5=Very Hot"),
    
    /** 
     * How often it rains.
     * 1=Very Dry, 2=Dry, 3=Normal, 4=Rainy, 5=Very Rainy
     */
    RAIN("Rain", PropertyCategory.WEATHER, DataType.INTEGER, 3, 1, 5,
            "Rain frequency. 1=Very Dry, 2=Dry, 3=Normal, 4=Rainy, 5=Very Rainy"),
    
    /** Maximum intensity of fog. 1=Normal, 2=Moderate, 3=Low, 4=None */
    MAX_FOG_INTENSITY("MaxFogIntensity", PropertyCategory.WEATHER, DataType.INTEGER, 1, 1, 4,
            "Maximum fog intensity. 1=Normal, 2=Moderate, 3=Low, 4=None"),
    
    /** Maximum intensity of rain. 1=Normal, 2=Moderate, 3=Low */
    MAX_RAIN_FX_INTENSITY("MaxRainFxIntensity", PropertyCategory.WEATHER, DataType.INTEGER, 1, 1, 3,
            "Maximum rain intensity. 1=Normal, 2=Moderate, 3=Low"),
    
    /** If snow will accumulate on the ground */
    ENABLE_SNOW_ON_GROUND("EnableSnowOnGround", PropertyCategory.WEATHER, DataType.BOOLEAN, true, null, null,
            "If snow will accumulate on the ground"),
    
    // ===== UTILITIES & SERVICES =====
    
    /** 
     * How long after start date that plumbing fixtures stop providing water.
     * 1=Instant, 2=0-30 Days, 3=0-2 Months, 4=0-6 Months, 5=0-1 Year, 6=0-5 Years, 7=2-6 Months, 8=6-12 Months, 9=Disabled
     */
    WATER_SHUT("WaterShut", PropertyCategory.UTILITIES, DataType.INTEGER, 2, 1, 9,
            "Water shutoff timing. 2=0-30 Days (default), 9=Disabled"),
    
    /** 
     * How long after start date that electricity turns off.
     * 1=Instant, 2=0-30 Days, 3=0-2 Months, 4=0-6 Months, 5=0-1 Year, 6=0-5 Years, 7=2-6 Months, 8=6-12 Months, 9=Disabled
     */
    ELEC_SHUT("ElecShut", PropertyCategory.UTILITIES, DataType.INTEGER, 2, 1, 9,
            "Electricity shutoff timing. 2=0-30 Days (default), 9=Disabled"),
    
    /** 
     * How long alarm batteries last after power shutoff.
     * 1=Instant, 2=0-30 Days, 3=0-2 Months, 4=0-6 Months, 5=0-1 Year, 6=0-5 Years
     */
    ALARM_DECAY("AlarmDecay", PropertyCategory.UTILITIES, DataType.INTEGER, 2, 1, 6,
            "How long alarm batteries last after power shutoff"),
    
    /** Water shutoff modifier in days. -1 to 2147483647 */
    WATER_SHUT_MODIFIER("WaterShutModifier", PropertyCategory.UTILITIES, DataType.INTEGER, 14, -1, 2147483647,
            "Days until water shuts off. -1=random based on WaterShut setting"),
    
    /** Electricity shutoff modifier in days. -1 to 2147483647 */
    ELEC_SHUT_MODIFIER("ElecShutModifier", PropertyCategory.UTILITIES, DataType.INTEGER, 14, -1, 2147483647,
            "Days until electricity shuts off. -1=random based on ElecShut setting"),
    
    /** Alarm decay modifier in days. -1 to 2147483647 */
    ALARM_DECAY_MODIFIER("AlarmDecayModifier", PropertyCategory.UTILITIES, DataType.INTEGER, 14, -1, 2147483647,
            "Days until alarm batteries die. -1=random based on AlarmDecay setting"),
    
    // ===== LOOT SETTINGS =====
    
    /** Food loot spawn rate. 0.00 to 4.00 */
    FOOD_LOOT_NEW("FoodLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Any food that can rot or spoil"),
    
    /** Literature loot spawn rate. 0.00 to 4.00 */
    LITERATURE_LOOT_NEW("LiteratureLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "All items that can be read, includes fliers"),
    
    /** Medical loot spawn rate. 0.00 to 4.00 */
    MEDICAL_LOOT_NEW("MedicalLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Medicine, bandages and first aid tools"),
    
    /** Survival gear loot spawn rate. 0.00 to 4.00 */
    SURVIVAL_GEARS_LOOT_NEW("SurvivalGearsLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Fishing Rods, Tents, camping gear etc."),
    
    /** Canned food loot spawn rate. 0.00 to 4.00 */
    CANNED_FOOD_LOOT_NEW("CannedFoodLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Canned and dried food, beverages"),
    
    /** Weapon loot spawn rate. 0.00 to 4.00 */
    WEAPON_LOOT_NEW("WeaponLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Weapons that are not tools in other categories"),
    
    /** Ranged weapon loot spawn rate. 0.00 to 4.00 */
    RANGED_WEAPON_LOOT_NEW("RangedWeaponLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Also includes weapon attachments"),
    
    /** Ammo loot spawn rate. 0.00 to 4.00 */
    AMMO_LOOT_NEW("AmmoLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Loose ammo, boxes and magazines"),
    
    /** Mechanics loot spawn rate. 0.00 to 4.00 */
    MECHANICS_LOOT_NEW("MechanicsLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Vehicle parts and the tools needed to install them"),
    
    /** Other loot spawn rate. 0.00 to 4.00 */
    OTHER_LOOT_NEW("OtherLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Everything else. Also affects foraging for all items in Town/Road zones"),
    
    /** Clothing loot spawn rate. 0.00 to 4.00 */
    CLOTHING_LOOT_NEW("ClothingLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "All wearable items that are not containers"),
    
    /** Container loot spawn rate. 0.00 to 4.00 */
    CONTAINER_LOOT_NEW("ContainerLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Backpacks and other wearable/equippable containers"),
    
    /** Key loot spawn rate. 0.00 to 4.00 */
    KEY_LOOT_NEW("KeyLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Keys for buildings/cars, key rings, and locks"),
    
    /** Media loot spawn rate. 0.00 to 4.00 */
    MEDIA_LOOT_NEW("MediaLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "VHS tapes and CDs"),
    
    /** Memento loot spawn rate. 0.00 to 4.00 */
    MEMENTO_LOOT_NEW("MementoLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Spiffo items, plushies, and other collectible keepsake items"),
    
    /** Cookware loot spawn rate. 0.00 to 4.00 */
    COOKWARE_LOOT_NEW("CookwareLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Items used in cooking, including knives. Does not include food"),
    
    /** Material loot spawn rate. 0.00 to 4.00 */
    MATERIAL_LOOT_NEW("MaterialLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Items used as ingredients for crafting or building. Does not include Tools"),
    
    /** Farming loot spawn rate. 0.00 to 4.00 */
    FARMING_LOOT_NEW("FarmingLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Items used in animal and plant agriculture, such as Seeds, Trowels, or Shovels"),
    
    /** Tool loot spawn rate. 0.00 to 4.00 */
    TOOL_LOOT_NEW("ToolLootNew", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.0, 4.0,
            "Tools that don't fit in other categories such as Mechanics or Farming"),
    
    /** Loot rolls multiplier. 0.10 to 100.00. WARNING: Can affect performance */
    ROLLS_MULTIPLIER("RollsMultiplier", PropertyCategory.LOOT, DataType.DOUBLE, 1.0, 0.1, 100.0,
            "Adjusts number of rolls on loot tables. Can negatively affect performance if set high"),
    
    /** Comma-separated list of items that won't spawn as loot */
    LOOT_ITEM_REMOVAL_LIST("LootItemRemovalList", PropertyCategory.LOOT, DataType.STRING, "", null, null,
            "Comma-separated list of item types that won't spawn as ordinary loot"),
    
    /** If items on removal list won't spawn in world stories */
    REMOVE_STORY_LOOT("RemoveStoryLoot", PropertyCategory.LOOT, DataType.BOOLEAN, true, null, null,
            "If items on Loot Item Removal List won't spawn in randomised world stories"),
    
    /** If items on removal list won't spawn on zombies */
    REMOVE_ZOMBIE_LOOT("RemoveZombieLoot", PropertyCategory.LOOT, DataType.BOOLEAN, true, null, null,
            "If items on Loot Item Removal List won't spawn worn by or attached to zombies"),
    
    /** Zombie population effect on loot spawning. 0 to 20 */
    ZOMBIE_POP_LOOT_EFFECT("ZombiePopLootEffect", PropertyCategory.LOOT, DataType.INTEGER, 10, 0, 20,
            "If greater than 0, loot spawn increases relative to nearby zombies, multiplied by this number"),
    
    /** Insane rarity loot factor. 0.00 to 0.20 */
    INSANE_LOOT_FACTOR("InsaneLootFactor", PropertyCategory.LOOT, DataType.DOUBLE, 0.05, 0.0, 0.2,
            "Loot factor for insane rarity items"),
    
    /** Extreme rarity loot factor. 0.05 to 0.60 */
    EXTREME_LOOT_FACTOR("ExtremeLootFactor", PropertyCategory.LOOT, DataType.DOUBLE, 0.2, 0.05, 0.6,
            "Loot factor for extreme rarity items"),
    
    /** Rare loot factor. 0.20 to 1.00 */
    RARE_LOOT_FACTOR("RareLootFactor", PropertyCategory.LOOT, DataType.DOUBLE, 0.6, 0.2, 1.0,
            "Loot factor for rare items"),
    
    /** Normal loot factor. 0.60 to 2.00 */
    NORMAL_LOOT_FACTOR("NormalLootFactor", PropertyCategory.LOOT, DataType.DOUBLE, 1.0, 0.6, 2.0,
            "Loot factor for normal rarity items"),
    
    /** Common loot factor. 1.00 to 3.00 */
    COMMON_LOOT_FACTOR("CommonLootFactor", PropertyCategory.LOOT, DataType.DOUBLE, 2.0, 1.0, 3.0,
            "Loot factor for common items"),
    
    /** Abundant loot factor. 2.00 to 4.00 */
    ABUNDANT_LOOT_FACTOR("AbundantLootFactor", PropertyCategory.LOOT, DataType.DOUBLE, 3.0, 2.0, 4.0,
            "Loot factor for abundant items"),
    
    /** Hours before loot respawn is prevented in visited zones. 0 to 2147483647 */
    SEEN_HOURS_PREVENT_LOOT_RESPAWN("SeenHoursPreventLootRespawn", PropertyCategory.LOOT, DataType.INTEGER, 0, 0, 2147483647,
            "Loot won't respawn in zones visited within this many hours. 0=disabled"),
    
    /** Hours before loot respawns in containers. 0 to 2147483647 */
    HOURS_FOR_LOOT_RESPAWN("HoursForLootRespawn", PropertyCategory.LOOT, DataType.INTEGER, 0, 0, 2147483647,
            "Hours before containers respawn loot. Must be looted once. 0=disabled"),
    
    /** Max items in container before it won't respawn. 0 to 2147483647 */
    MAX_ITEMS_FOR_LOOT_RESPAWN("MaxItemsForLootRespawn", PropertyCategory.LOOT, DataType.INTEGER, 5, 0, 2147483647,
            "Containers with this many or more items won't respawn loot"),
    
    /** If construction prevents loot respawn */
    CONSTRUCTION_PREVENTS_LOOT_RESPAWN("ConstructionPreventsLootRespawn", PropertyCategory.LOOT, DataType.BOOLEAN, false, null, null,
            "Items won't respawn in buildings that players have barricaded or built in"),
    
    /** Maximum chance a building is already looted. 0 to 200 */
    MAXIMUM_LOOTED("MaximumLooted", PropertyCategory.LOOT, DataType.INTEGER, 50, 0, 200,
            "The chance that any building will already be looted when found"),
    
    /** Days until maximum looted chance is reached. 0 to 3650 */
    DAYS_UNTIL_MAXIMUM_LOOTED("DaysUntilMaximumLooted", PropertyCategory.LOOT, DataType.INTEGER, 90, 0, 3650,
            "How long it takes for Maximum Looted Building Chance to be reached"),
    
    /** Rural building looted multiplier. 0.00 to 2.00 */
    RURAL_LOOTED("RuralLooted", PropertyCategory.LOOT, DataType.DOUBLE, 0.5, 0.0, 2.0,
            "The chance that any rural building will already be looted when found"),
    
    /** Maximum diminished loot percentage. 0 to 100 */
    MAXIMUM_DIMINISHED_LOOT("MaximumDiminishedLoot", PropertyCategory.LOOT, DataType.INTEGER, 0, 0, 100,
            "Maximum loot that won't spawn when Days Until Maximum Diminished Loot is reached"),
    
    /** Days until maximum diminished loot. 0 to 3650 */
    DAYS_UNTIL_MAXIMUM_DIMINISHED_LOOT("DaysUntilMaximumDiminishedLoot", PropertyCategory.LOOT, DataType.INTEGER, 3650, 0, 3650,
            "How long it takes for Maximum Diminished Loot Percentage to be reached"),
    
    // ===== WORLD & ENVIRONMENT =====
    
    /** 
     * Erosion speed (vines, long grass, new trees).
     * 1=Very Fast (20 Days), 2=Fast (50 Days), 3=Normal (100 Days), 4=Slow (200 Days), 5=Very Slow (500 Days)
     */
    EROSION_SPEED("ErosionSpeed", PropertyCategory.WORLD, DataType.INTEGER, 3, 1, 5,
            "Erosion system growth speed. 3=Normal (100 Days)"),
    
    /** Custom erosion days. 0=use ErosionSpeed setting. 0 to 36500 */
    EROSION_DAYS("ErosionDays", PropertyCategory.WORLD, DataType.INTEGER, 0, -1, 36500,
            "Custom erosion speed in days. 0=use ErosionSpeed option, max=36500 (100 years)"),
    
    /** 
     * Speed of plant growth.
     * 1=Very Fast, 2=Fast, 3=Normal, 4=Slow, 5=Very Slow
     */
    FARMING("Farming", PropertyCategory.WORLD, DataType.INTEGER, 3, 1, 5,
            "Speed of plant growth. 1=Very Fast, 3=Normal, 5=Very Slow"),
    
    /** 
     * How long food takes to compost.
     * 1=1 Week, 2=2 Weeks, 3=3 Weeks, 4=4 Weeks, 5=6 Weeks, 6=8 Weeks, 7=10 Weeks, 8=12 Weeks
     */
    COMPOST_TIME("CompostTime", PropertyCategory.WORLD, DataType.INTEGER, 2, 1, 8,
            "How long food takes to break down in composter. 2=2 Weeks (default)"),
    
    /** 
     * Abundance of foraged items.
     * 1=Very Poor, 2=Poor, 3=Normal, 4=Abundant, 5=Very Abundant
     */
    NATURE_ABUNDANCE("NatureAbundance", PropertyCategory.WORLD, DataType.INTEGER, 3, 1, 5,
            "Abundance of items found in Foraging mode"),
    
    /** Days before rotten food is removed. -1 to 2147483647 */
    DAYS_FOR_ROTTEN_FOOD_REMOVAL("DaysForRottenFoodRemoval", PropertyCategory.WORLD, DataType.INTEGER, -1, -1, 2147483647,
            "Days before rotten food is removed from map. -1=never removed"),
    
    /** 
     * Chance of finding randomized buildings.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often, 7=Always Tries
     */
    SURVIVOR_HOUSE_CHANCE("SurvivorHouseChance", PropertyCategory.WORLD, DataType.INTEGER, 3, 1, 7,
            "Chance of finding randomized buildings like burnt houses or loot stashes"),
    
    /** 
     * Chance of road stories spawning.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often, 7=Always Tries
     */
    VEHICLE_STORY_CHANCE("VehicleStoryChance", PropertyCategory.WORLD, DataType.INTEGER, 3, 1, 7,
            "Chance of road stories like police roadblocks spawning"),
    
    /** 
     * Chance of zone-specific stories spawning.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often, 7=Always Tries
     */
    ZONE_STORY_CHANCE("ZoneStoryChance", PropertyCategory.WORLD, DataType.INTEGER, 3, 1, 7,
            "Chance of zone-specific stories like forest campsites spawning"),
    
    /** Comma-separated list of items to remove after HoursForWorldItemRemoval */
    WORLD_ITEM_REMOVAL_LIST("WorldItemRemovalList", PropertyCategory.WORLD, DataType.STRING, "Base.Hat,Base.Glasses,Base.Maggots", null, null,
            "Comma-separated list of item types removed after HoursForWorldItemRemoval hours"),
    
    /** Hours before world items are removed. 0.00 to 2147483647.00 */
    HOURS_FOR_WORLD_ITEM_REMOVAL("HoursForWorldItemRemoval", PropertyCategory.WORLD, DataType.DOUBLE, 24.0, 0.0, 2147483647.0,
            "Hours before dropped items are removed. 0=never removed"),
    
    /** If true, items NOT in WorldItemRemovalList are removed instead */
    ITEM_REMOVAL_LIST_BLACKLIST_TOGGLE("ItemRemovalListBlacklistToggle", PropertyCategory.WORLD, DataType.BOOLEAN, false, null, null,
            "If true, items NOT in WorldItemRemovalList will be removed"),
    
    /** Days before blood splats are removed. 0 to 365 */
    BLOOD_SPLAT_LIFESPAN_DAYS("BloodSplatLifespanDays", PropertyCategory.WORLD, DataType.INTEGER, 0, 0, 365,
            "Days before old blood splats removed. 0=never disappear"),
    
    /** If buildings have more than this many rooms they won't be looted. 0 to 200 */
    MAXIMUM_LOOTED_BUILDING_ROOMS("MaximumLootedBuildingRooms", PropertyCategory.WORLD, DataType.INTEGER, 50, 0, 200,
            "Buildings with more than this many rooms won't be looted"),
    
    // ===== PLAYER & SURVIVAL =====
    
    /** 
     * How fast hunger, thirst, fatigue decrease.
     * 1=Very Fast, 2=Fast, 3=Normal, 4=Slow, 5=Very Slow
     */
    STATS_DECREASE("StatsDecrease", PropertyCategory.PLAYER, DataType.INTEGER, 3, 1, 5,
            "How fast hunger, thirst, and fatigue decrease"),
    
    /** 
     * How likely house alarms trigger on break-in.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    ALARM("Alarm", PropertyCategory.PLAYER, DataType.INTEGER, 4, 1, 6,
            "How likely to activate a house alarm when breaking in"),
    
    /** 
     * How frequently doors are locked.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    LOCKED_HOUSES("LockedHouses", PropertyCategory.PLAYER, DataType.INTEGER, 6, 1, 6,
            "How frequently doors of homes/buildings are locked"),
    
    /** Spawn with starter kit (chips, water, backpack, bat, hammer) */
    STARTER_KIT("StarterKit", PropertyCategory.PLAYER, DataType.BOOLEAN, false, null, null,
            "Spawn with Chips, Water Bottle, Small Backpack, Baseball Bat, and Hammer"),
    
    /** If nutritional value affects player condition */
    NUTRITION("Nutrition", PropertyCategory.PLAYER, DataType.BOOLEAN, true, null, null,
            "Nutritional value affects player condition. Off=no weight gain/loss"),
    
    /** 
     * How fast food spoils.
     * 1=Very Fast, 2=Fast, 3=Normal, 4=Slow, 5=Very Slow
     */
    FOOD_ROT_SPEED("FoodRotSpeed", PropertyCategory.PLAYER, DataType.INTEGER, 3, 1, 5,
            "How fast food spoils inside or outside fridge"),
    
    /** 
     * How effective fridges are.
     * 1=Very Low, 2=Low, 3=Normal, 4=High, 5=Very High, 6=No decay
     */
    FRIDGE_FACTOR("FridgeFactor", PropertyCategory.PLAYER, DataType.INTEGER, 3, 1, 6,
            "How effective fridges are at keeping food fresh"),
    
    /** 
     * Recovery from tiredness.
     * 1=Very Fast, 2=Fast, 3=Normal, 4=Slow, 5=Very Slow
     */
    END_REGEN("EndRegen", PropertyCategory.PLAYER, DataType.INTEGER, 3, 1, 5,
            "Recovery from being tired after performing actions"),
    
    /** Free character creation points. -100 to 100 */
    CHARACTER_FREE_POINTS("CharacterFreePoints", PropertyCategory.PLAYER, DataType.INTEGER, 0, -100, 100,
            "Adds free points during character creation"),
    
    /** If survivors can get broken limbs */
    BONE_FRACTURE("BoneFracture", PropertyCategory.PLAYER, DataType.BOOLEAN, true, null, null,
            "If survivors can get broken limbs from impacts, zombie damage, falls etc."),
    
    /** 
     * Impact of injuries and healing time.
     * 1=Low, 2=Normal, 3=High
     */
    INJURY_SEVERITY("InjurySeverity", PropertyCategory.PLAYER, DataType.INTEGER, 2, 1, 3,
            "Impact of injuries on body and healing time"),
    
    /** Muscle strain multiplier. 0.00 to 10.00 */
    MUSCLE_STRAIN_FACTOR("MuscleStrainFactor", PropertyCategory.PLAYER, DataType.DOUBLE, 1.0, 0.0, 10.0,
            "Multiplier for muscle strain from swinging weapons or carrying heavy loads"),
    
    /** Discomfort multiplier. 0.00 to 10.00 */
    DISCOMFORT_FACTOR("DiscomfortFactor", PropertyCategory.PLAYER, DataType.DOUBLE, 1.0, 0.0, 10.0,
            "Multiplier for discomfort from worn items"),
    
    /** Wound infection damage factor. 0.00 to 10.00 */
    WOUND_INFECTION_FACTOR("WoundInfectionFactor", PropertyCategory.PLAYER, DataType.DOUBLE, 0.0, 0.0, 10.0,
            "If greater than zero, damage can be taken from serious wound infections"),
    
    /** If randomized clothing tints won't be virtually black */
    NO_BLACK_CLOTHES("NoBlackClothes", PropertyCategory.PLAYER, DataType.BOOLEAN, true, null, null,
            "Clothing with randomized tints won't be so dark to be virtually black"),
    
    /** Disables failure chances when climbing */
    EASY_CLIMBING("EasyClimbing", PropertyCategory.PLAYER, DataType.BOOLEAN, false, null, null,
            "Disables failure chances when climbing sheet ropes or over walls"),
    
    /** All clothing unlocked for character creation */
    ALL_CLOTHES_UNLOCKED("AllClothesUnlocked", PropertyCategory.PLAYER, DataType.BOOLEAN, false, null, null,
            "Select from every piece of clothing when customizing character"),
    
    /** If tainted water shows a warning */
    ENABLE_TAINTED_WATER_TEXT("EnableTaintedWaterText", PropertyCategory.PLAYER, DataType.BOOLEAN, true, null, null,
            "If tainted water will show a warning marking it as such"),
    
    /** 
     * If poison can be added to food.
     * 1=True, 2=False, 3=Only bleach poisoning is disabled
     */
    ENABLE_POISONING("EnablePoisoning", PropertyCategory.PLAYER, DataType.INTEGER, 1, 1, 3,
            "If poison can be added to food. 1=True, 2=False, 3=Only bleach disabled"),
    
    /** 
     * Negative traits penalty system.
     * 1=None, 2=1 point per 3 traits, 3=1 point per 2 traits, 4=1 point per trait after first
     */
    NEGATIVE_TRAITS_PENALTY("NegativeTraitsPenalty", PropertyCategory.PLAYER, DataType.INTEGER, 1, 1, 4,
            "Diminishing returns on bonus trait points from multiple negative traits"),
    
    /** Minutes to read one page. 0.00 to 60.00 */
    MINUTES_PER_PAGE("MinutesPerPage", PropertyCategory.PLAYER, DataType.DOUBLE, 2.0, 0.0, 60.0,
            "In-game minutes it takes to read one page of a skill book"),
    
    /** Days before literature can be re-read for benefits. 1 to 365 */
    LITERATURE_COOLDOWN("LiteratureCooldown", PropertyCategory.PLAYER, DataType.INTEGER, 90, 1, 365,
            "Days before one can benefit from reading previously read literature"),
    
    // ===== EVENTS & META =====
    
    /** 
     * How regularly helicopters pass over.
     * 1=Never, 2=Once, 3=Sometimes, 4=Often
     */
    HELICOPTER("Helicopter", PropertyCategory.EVENTS, DataType.INTEGER, 2, 1, 4,
            "How regularly a helicopter passes over the Event Zone"),
    
    /** 
     * How often metagame events occur.
     * 1=Never, 2=Sometimes, 3=Often
     */
    META_EVENT("MetaEvent", PropertyCategory.EVENTS, DataType.INTEGER, 2, 1, 3,
            "How often zombie-attracting metagame events like distant gunshots occur"),
    
    /** 
     * How often sleep events occur.
     * 1=Never, 2=Sometimes, 3=Often
     */
    SLEEPING_EVENT("SleepingEvent", PropertyCategory.EVENTS, DataType.INTEGER, 1, 1, 3,
            "How often events during player sleep, like nightmares, occur"),
    
    /** 
     * How often annotated maps spawn.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    ANNOTATED_MAP_CHANCE("AnnotatedMapChance", PropertyCategory.EVENTS, DataType.INTEGER, 4, 1, 6,
            "How often a looted map has notes written by deceased survivor"),
    
    /** 
     * If/when maggots spawn in corpses.
     * 1=In and Around Bodies, 2=In Bodies Only, 3=Never
     */
    MAGGOT_SPAWN("MaggotSpawn", PropertyCategory.EVENTS, DataType.INTEGER, 1, 1, 3,
            "If/when maggots can spawn in corpses"),
    
    /** 
     * Media knowledge display mode.
     * 1=Fully revealed, 2=Shown as ???, 3=Completely hidden
     */
    META_KNOWLEDGE("MetaKnowledge", PropertyCategory.EVENTS, DataType.INTEGER, 3, 1, 3,
            "How unseen media content is displayed"),
    
    /** If recipes can be seen without learning them */
    SEE_NOT_LEARNT_RECIPE("SeeNotLearntRecipe", PropertyCategory.EVENTS, DataType.BOOLEAN, true, null, null,
            "If you can see recipes for a station even if you haven't learnt them yet"),
    
    // ===== GENERATORS =====
    
    /** Fuel consumption per in-game hour. 0.00 to 100.00 */
    GENERATOR_FUEL_CONSUMPTION("GeneratorFuelConsumption", PropertyCategory.GENERATOR, DataType.DOUBLE, 0.1, 0.0, 100.0,
            "How much fuel generators consume per in-game hour"),
    
    /** 
     * Generator spawn chance.
     * 1=None, 2=Insanely Rare, 3=Extremely Rare, 4=Rare, 5=Normal, 6=Common, 7=Abundant
     */
    GENERATOR_SPAWNING("GeneratorSpawning", PropertyCategory.GENERATOR, DataType.INTEGER, 4, 1, 7,
            "Chance of electrical generators spawning on the map"),
    
    /** If generators work on exterior tiles */
    ALLOW_EXTERIOR_GENERATOR("AllowExteriorGenerator", PropertyCategory.GENERATOR, DataType.BOOLEAN, true, null, null,
            "If generators work on exterior tiles, allowing powering of gas pumps"),
    
    /** Generator horizontal power range. 1 to 100 */
    GENERATOR_TILE_RANGE("GeneratorTileRange", PropertyCategory.GENERATOR, DataType.INTEGER, 20, 1, 100,
            "Tile range of generator power"),
    
    /** Generator vertical power range. 1 to 15 */
    GENERATOR_VERTICAL_POWER_RANGE("GeneratorVerticalPowerRange", PropertyCategory.GENERATOR, DataType.INTEGER, 3, 1, 15,
            "How many levels above and below a generator can provide electricity"),
    
    // ===== CONSTRUCTION & STRUCTURES =====
    
    /** 
     * Construction hit point bonus.
     * 1=Very Low, 2=Low, 3=Normal, 4=High, 5=Very High
     */
    CONSTRUCTION_BONUS_POINTS("ConstructionBonusPoints", PropertyCategory.CONSTRUCTION, DataType.INTEGER, 3, 1, 5,
            "Extra hit points for player constructions vs zombie damage"),
    
    /** Maximum fuel hours for campfire/stove. 1 to 168 */
    MAXIMUM_FIRE_FUEL_HOURS("MaximumFireFuelHours", PropertyCategory.CONSTRUCTION, DataType.INTEGER, 8, 1, 168,
            "Maximum hours of fuel that can be placed in campfire, wood stove etc."),
    
    // ===== NIGHT & LIGHTING =====
    
    /** 
     * Night ambient lighting level.
     * 1=Pitch Black, 2=Dark, 3=Normal, 4=Bright
     */
    NIGHT_DARKNESS("NightDarkness", PropertyCategory.NIGHT, DataType.INTEGER, 3, 1, 4,
            "Level of ambient lighting at night"),
    
    /** 
     * Night duration.
     * 1=Always Night, 2=Long, 3=Normal, 4=Short, 5=Always Day
     */
    NIGHT_LENGTH("NightLength", PropertyCategory.NIGHT, DataType.INTEGER, 3, 1, 5,
            "Time from dusk to dawn"),
    
    /** Lightbulb lifespan multiplier. 0.00 to 1000.00 */
    LIGHT_BULB_LIFESPAN("LightBulbLifespan", PropertyCategory.NIGHT, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Higher value=longer lightbulb life. 0=never break. Doesn't affect vehicle headlights"),
    
    // ===== CORPSES & GORE =====
    
    /** Hours before corpses are removed. -1.00 to 2147483647.00 */
    HOURS_FOR_CORPSE_REMOVAL("HoursForCorpseRemoval", PropertyCategory.CORPSES, DataType.DOUBLE, 216.0, -1.0, 2147483647.0,
            "Hours before dead zombie bodies disappear. If 0, maggots won't spawn on corpses"),
    
    /** 
     * Impact of decaying bodies.
     * 1=None, 2=Low, 3=Normal, 4=High, 5=Insane
     */
    DECAYING_CORPSE_HEALTH_IMPACT("DecayingCorpseHealthImpact", PropertyCategory.CORPSES, DataType.INTEGER, 3, 1, 5,
            "Impact of nearby decaying bodies on player health and emotions"),
    
    /** If living zombies impact health like corpses */
    ZOMBIE_HEALTH_IMPACT("ZombieHealthImpact", PropertyCategory.CORPSES, DataType.BOOLEAN, false, null, null,
            "Whether nearby living zombies have same impact on health and emotions"),
    
    /** 
     * Blood spray level.
     * 1=None, 2=Low, 3=Normal, 4=High, 5=Ultra Gore
     */
    BLOOD_LEVEL("BloodLevel", PropertyCategory.CORPSES, DataType.INTEGER, 3, 1, 5,
            "How much blood is sprayed on floors and walls by injuries"),
    
    /** 
     * Clothing degradation speed.
     * 1=Disabled, 2=Slow, 3=Normal, 4=Fast
     */
    CLOTHING_DEGRADATION("ClothingDegradation", PropertyCategory.CORPSES, DataType.INTEGER, 3, 1, 4,
            "How quickly clothing degrades, becomes dirty, and bloodied"),
    
    // ===== FIRE =====
    
    /** If fires spread when started */
    FIRE_SPREAD("FireSpread", PropertyCategory.FIRE, DataType.BOOLEAN, true, null, null,
            "If fires spread when started"),
    
    // ===== VEHICLES =====
    
    /** If vehicles spawn */
    ENABLE_VEHICLES("EnableVehicles", PropertyCategory.VEHICLE, DataType.BOOLEAN, true, null, null,
            "If vehicles will spawn"),
    
    /** 
     * Vehicle spawn rate.
     * 1=None, 2=Very Low, 3=Low, 4=Normal, 5=High
     */
    CAR_SPAWN_RATE("CarSpawnRate", PropertyCategory.VEHICLE, DataType.INTEGER, 3, 1, 5,
            "How frequently vehicles can be discovered on the map"),
    
    /** Zombie attraction multiplier for engines. 0.00 to 100.00 */
    ZOMBIE_ATTRACTION_MULTIPLIER("ZombieAttractionMultiplier", PropertyCategory.VEHICLE, DataType.DOUBLE, 1.0, 0.0, 100.0,
            "General engine loudness to zombies"),
    
    /** If vehicles don't need keys to start */
    VEHICLE_EASY_USE("VehicleEasyUse", PropertyCategory.VEHICLE, DataType.BOOLEAN, false, null, null,
            "Whether found vehicles are locked, need keys to start etc."),
    
    /** 
     * Initial gas tank level.
     * 1=Very Low, 2=Low, 3=Normal, 4=High, 5=Very High, 6=Full
     */
    INITIAL_GAS("InitialGas", PropertyCategory.VEHICLE, DataType.INTEGER, 2, 1, 6,
            "How full the gas tank of discovered vehicles will be"),
    
    /** If gas pumps never run out */
    FUEL_STATION_GAS_INFINITE("FuelStationGasInfinite", PropertyCategory.VEHICLE, DataType.BOOLEAN, false, null, null,
            "If enabled, gas pumps will never run out of fuel"),
    
    /** Minimum gas in pumps. 0.00 to 1.00 */
    FUEL_STATION_GAS_MIN("FuelStationGasMin", PropertyCategory.VEHICLE, DataType.DOUBLE, 0.0, 0.0, 1.0,
            "Minimum amount of gasoline that can spawn in gas pumps"),
    
    /** Maximum gas in pumps. 0.00 to 1.00 */
    FUEL_STATION_GAS_MAX("FuelStationGasMax", PropertyCategory.VEHICLE, DataType.DOUBLE, 0.7, 0.0, 1.0,
            "Maximum amount of gasoline that can spawn in gas pumps"),
    
    /** Percentage chance pump is empty. 0 to 100 */
    FUEL_STATION_GAS_EMPTY_CHANCE("FuelStationGasEmptyChance", PropertyCategory.VEHICLE, DataType.INTEGER, 20, 0, 100,
            "Chance as percentage that individual gas pumps initially have no fuel"),
    
    /** 
     * How likely cars are locked.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    LOCKED_CAR("LockedCar", PropertyCategory.VEHICLE, DataType.INTEGER, 3, 1, 6,
            "How likely cars will be locked"),
    
    /** Gas consumption multiplier. 0.00 to 100.00 */
    CAR_GAS_CONSUMPTION("CarGasConsumption", PropertyCategory.VEHICLE, DataType.DOUBLE, 1.0, 0.0, 100.0,
            "How gas-hungry vehicles are"),
    
    /** 
     * General vehicle condition.
     * 1=Very Low, 2=Low, 3=Normal, 4=High, 5=Very High
     */
    CAR_GENERAL_CONDITION("CarGeneralCondition", PropertyCategory.VEHICLE, DataType.INTEGER, 2, 1, 5,
            "General condition discovered vehicles will be in"),
    
    /** 
     * Crash damage to vehicles.
     * 1=Very Low, 2=Low, 3=Normal, 4=High, 5=Very High
     */
    CAR_DAMAGE_ON_IMPACT("CarDamageOnImpact", PropertyCategory.VEHICLE, DataType.INTEGER, 3, 1, 5,
            "Amount of damage dealt to vehicles that crash"),
    
    /** 
     * Player damage from being hit by car.
     * 1=None, 2=Low, 3=Normal, 4=High, 5=Very High
     */
    DAMAGE_TO_PLAYER_FROM_HIT_BY_A_CAR("DamageToPlayerFromHitByACar", PropertyCategory.VEHICLE, DataType.INTEGER, 1, 1, 5,
            "Damage received by player from being crashed into"),
    
    /** If traffic jams spawn */
    TRAFFIC_JAM("TrafficJam", PropertyCategory.VEHICLE, DataType.BOOLEAN, true, null, null,
            "If traffic jams consisting of wrecked cars appear on main roads"),
    
    /** 
     * Vehicle alarm frequency.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    CAR_ALARM("CarAlarm", PropertyCategory.VEHICLE, DataType.INTEGER, 2, 1, 6,
            "How frequently discovered vehicles have active alarms"),
    
    /** If player can be injured in crashes */
    PLAYER_DAMAGE_FROM_CRASH("PlayerDamageFromCrash", PropertyCategory.VEHICLE, DataType.BOOLEAN, true, null, null,
            "If player can get injured from being in car accident"),
    
    /** Hours before siren shuts off. 0.00 to 168.00 */
    SIREN_SHUTOFF_HOURS("SirenShutoffHours", PropertyCategory.VEHICLE, DataType.DOUBLE, 0.0, 0.0, 168.0,
            "How many in-game hours before a wailing siren shuts off"),
    
    /** 
     * Chance vehicle has gas.
     * 1=Low, 2=Normal, 3=High
     */
    CHANCE_HAS_GAS("ChanceHasGas", PropertyCategory.VEHICLE, DataType.INTEGER, 1, 1, 3,
            "Chance of finding a vehicle with gas in its tank"),
    
    /** 
     * Recently maintained vehicle chance.
     * 1=None, 2=Low, 3=Normal, 4=High
     */
    RECENTLY_SURVIVOR_VEHICLES("RecentlySurvivorVehicles", PropertyCategory.VEHICLE, DataType.INTEGER, 2, 1, 4,
            "Whether player can discover cars cared for after Knox infection struck"),
    
    // ===== COMBAT =====
    
    /** If melee attacking slows you down */
    ATTACK_BLOCK_MOVEMENTS("AttackBlockMovements", PropertyCategory.COMBAT, DataType.BOOLEAN, true, null, null,
            "If melee attacking slows you down"),
    
    /** If melee weapons can hit multiple zombies */
    MULTI_HIT_ZOMBIES("MultiHitZombies", PropertyCategory.COMBAT, DataType.BOOLEAN, true, null, null,
            "If certain melee weapons can strike multiple zombies in one hit"),
    
    /** 
     * Rear attack bite chance.
     * 1=Low, 2=Medium, 3=High
     */
    REAR_VULNERABILITY("RearVulnerability", PropertyCategory.COMBAT, DataType.INTEGER, 3, 1, 3,
            "Chance of being bitten when zombie attacks from behind"),
    
    // ===== ZOMBIES - LORE =====
    
    /** If sirens attract zombies */
    SIREN_EFFECTS_ZOMBIES("SirenEffectsZombies", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, true, null, null,
            "If zombies will head towards the sound of vehicle sirens"),
    
    // ===== ANIMALS =====
    
    /** 
     * Animal stats reduction speed.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_STATS_MODIFIER("AnimalStatsModifier", PropertyCategory.ANIMALS, DataType.INTEGER, 4, 1, 6,
            "Speed at which animal stats (hunger, thirst etc.) reduce"),
    
    /** 
     * Animal meta stats reduction speed.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_META_STATS_MODIFIER("AnimalMetaStatsModifier", PropertyCategory.ANIMALS, DataType.INTEGER, 4, 1, 6,
            "Speed at which animal stats reduce while in meta"),
    
    /** 
     * Animal pregnancy duration.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_PREGNANCY_TIME("AnimalPregnancyTime", PropertyCategory.ANIMALS, DataType.INTEGER, 2, 1, 6,
            "How long animals will be pregnant before giving birth"),
    
    /** 
     * Animal aging speed.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_AGE_MODIFIER("AnimalAgeModifier", PropertyCategory.ANIMALS, DataType.INTEGER, 3, 1, 6,
            "Speed at which animals age"),
    
    /** 
     * Animal milk increase speed.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_MILK_INC_MODIFIER("AnimalMilkIncModifier", PropertyCategory.ANIMALS, DataType.INTEGER, 3, 1, 6,
            "Speed at which animals produce milk"),
    
    /** 
     * Animal wool growth speed.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_WOOL_INC_MODIFIER("AnimalWoolIncModifier", PropertyCategory.ANIMALS, DataType.INTEGER, 3, 1, 6,
            "Speed at which animals grow wool"),
    
    /** 
     * Chance of finding animals on farms.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often, 7=Always
     */
    ANIMAL_RANCH_CHANCE("AnimalRanchChance", PropertyCategory.ANIMALS, DataType.INTEGER, 7, 1, 7,
            "Chance of finding animals in farm"),
    
    /** Hours for grass regrowth. 1 to 9999 */
    ANIMAL_GRASS_REGROW_TIME("AnimalGrassRegrowTime", PropertyCategory.ANIMALS, DataType.INTEGER, 240, 1, 9999,
            "Hours grass takes to regrow after being eaten or cut"),
    
    /** If meta foxes can attack chickens */
    ANIMAL_META_PREDATOR("AnimalMetaPredator", PropertyCategory.ANIMALS, DataType.BOOLEAN, false, null, null,
            "If meta fox may attack chickens if hutch door left open at night"),
    
    /** If animals respect mating seasons */
    ANIMAL_MATING_SEASON("AnimalMatingSeason", PropertyCategory.ANIMALS, DataType.BOOLEAN, true, null, null,
            "If animals with mating season respect it. Otherwise reproduce all year"),
    
    /** 
     * Egg hatch time.
     * 1=Ultra Fast, 2=Very Fast, 3=Fast, 4=Normal, 5=Slow, 6=Very Slow
     */
    ANIMAL_EGG_HATCH("AnimalEggHatch", PropertyCategory.ANIMALS, DataType.INTEGER, 3, 1, 6,
            "How long before baby animals hatch from eggs"),
    
    /** If animal calls attract zombies */
    ANIMAL_SOUND_ATTRACT_ZOMBIES("AnimalSoundAttractZombies", PropertyCategory.ANIMALS, DataType.BOOLEAN, false, null, null,
            "If animal calls will attract nearby zombies"),
    
    /** 
     * Animal track spawn chance.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    ANIMAL_TRACK_CHANCE("AnimalTrackChance", PropertyCategory.ANIMALS, DataType.INTEGER, 4, 1, 6,
            "Chance of animals leaving tracks"),
    
    /** 
     * Animal path spawn chance.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often
     */
    ANIMAL_PATH_CHANCE("AnimalPathChance", PropertyCategory.ANIMALS, DataType.INTEGER, 4, 1, 6,
            "Chance of creating a path for animals to be hunted"),
    
    // ===== VERMIN & FISHING =====
    
    /** Maximum rat infestation index. 0 to 50 */
    MAXIMUM_RAT_INDEX("MaximumRatIndex", PropertyCategory.VERMIN, DataType.INTEGER, 25, 0, 50,
            "Frequency and intensity of rats in infested buildings"),
    
    /** Days until maximum rat index. 0 to 365 */
    DAYS_UNTIL_MAXIMUM_RAT_INDEX("DaysUntilMaximumRatIndex", PropertyCategory.VERMIN, DataType.INTEGER, 90, 0, 365,
            "How long it takes for Maximum Vermin Index to be reached"),
    
    /** 
     * Fish abundance.
     * 1=Very Poor, 2=Poor, 3=Normal, 4=Abundant, 5=Very Abundant
     */
    FISH_ABUNDANCE("FishAbundance", PropertyCategory.FISHING, DataType.INTEGER, 3, 1, 5,
            "Abundance of fish in rivers and lakes"),
    
    // ===== FARMING & CRAFTING =====
    
    /** 
     * Plant water loss and disease resistance.
     * 1=Very High, 2=High, 3=Normal, 4=Low, 5=Very Low
     */
    PLANT_RESILIENCE("PlantResilience", PropertyCategory.FARMING, DataType.INTEGER, 3, 1, 5,
            "How much water plants lose per day and ability to avoid disease"),
    
    /** 
     * Plant harvest yield.
     * 1=Very Poor, 2=Poor, 3=Normal, 4=Abundant, 5=Very Abundant
     */
    PLANT_ABUNDANCE("PlantAbundance", PropertyCategory.FARMING, DataType.INTEGER, 3, 1, 5,
            "Yield of plants when harvested"),
    
    /** If crops grown inside buildings die */
    KILL_INSIDE_CROPS("KillInsideCrops", PropertyCategory.FARMING, DataType.BOOLEAN, true, null, null,
            "When enabled, crops and herbs grown inside buildings will die. Doesn't affect houseplants"),
    
    /** If plant growth is affected by seasons */
    PLANT_GROWING_SEASONS("PlantGrowingSeasons", PropertyCategory.FARMING, DataType.BOOLEAN, true, null, null,
            "When enabled, growth of plants is affected by seasons"),
    
    /** If dirt can be placed above ground for farming. WARNING: Can cause performance issues */
    PLACE_DIRT_ABOVEGROUND("PlaceDirtAboveground", PropertyCategory.FARMING, DataType.BOOLEAN, false, null, null,
            "When enabled, dirt can be placed and farming performed above ground level. Can cause performance issues"),
    
    /** Plant growth speed multiplier. 0.10 to 100.00 */
    FARMING_SPEED_NEW("FarmingSpeedNew", PropertyCategory.FARMING, DataType.DOUBLE, 1.0, 0.1, 100.0,
            "Speed of plant growth"),
    
    /** Harvested crop abundance multiplier. 0.10 to 10.00 */
    FARMING_AMOUNT_NEW("FarmingAmountNew", PropertyCategory.FARMING, DataType.DOUBLE, 1.0, 0.1, 10.0,
            "Abundance of harvested crops"),
    
    /** Clay lake spawn chance. 0.00 to 1.00 */
    CLAY_LAKE_CHANCE("ClayLakeChance", PropertyCategory.CRAFTING, DataType.DOUBLE, 0.05, 0.0, 1.0,
            "Chance to turn dirt floor into clay floor in lakes"),
    
    /** Clay river spawn chance. 0.00 to 1.00 */
    CLAY_RIVER_CHANCE("ClayRiverChance", PropertyCategory.CRAFTING, DataType.DOUBLE, 0.05, 0.0, 1.0,
            "Chance to turn dirt floor into clay floor in rivers"),
    
    // ===== SKILLS & XP =====
    
    /** Skill level where media stops giving XP. 0 to 10 */
    LEVEL_FOR_MEDIA_XP_CUTOFF("LevelForMediaXPCutoff", PropertyCategory.SKILLS, DataType.INTEGER, 3, 0, 10,
            "When skill is at this level or above, television/VHS/media won't provide XP"),
    
    /** Skill level where dismantling stops giving XP. 0 to 10 */
    LEVEL_FOR_DISMANTLE_XP_CUTOFF("LevelForDismantleXPCutoff", PropertyCategory.SKILLS, DataType.INTEGER, 0, 0, 10,
            "When skill at this level or above, scrapping furniture doesn't give XP. Doesn't apply to Electrical"),
    
    // ===== FIREARMS =====
    
    /** If firearms use damage chance instead of hit chance */
    FIREARM_USE_DAMAGE_CHANCE("FirearmUseDamageChance", PropertyCategory.FIREARMS, DataType.BOOLEAN, true, null, null,
            "Replaces Chance-To-Hit with Chance-To-Damage. Prioritizes player aiming"),
    
    /** Gunshot hearing distance multiplier. 0.20 to 2.00 */
    FIREARM_NOISE_MULTIPLIER("FirearmNoiseMultiplier", PropertyCategory.FIREARMS, DataType.DOUBLE, 1.0, 0.2, 2.0,
            "Multiplier for distance at which zombies can hear gunshots"),
    
    /** Firearm jamming multiplier. 0.00 to 10.00 */
    FIREARM_JAM_MULTIPLIER("FirearmJamMultiplier", PropertyCategory.FIREARMS, DataType.DOUBLE, 0.0, 0.0, 10.0,
            "Multiplier for firearm jamming chance. 0=disabled"),
    
    /** Moodle effect on hit chance. 0.00 to 10.00 */
    FIREARM_MOODLE_MULTIPLIER("FirearmMoodleMultiplier", PropertyCategory.FIREARMS, DataType.DOUBLE, 1.0, 0.0, 10.0,
            "Multiplier for Moodle effects on hit chance. 0=disabled"),
    
    /** Weather effect on hit chance. 0.00 to 10.00 */
    FIREARM_WEATHER_MULTIPLIER("FirearmWeatherMultiplier", PropertyCategory.FIREARMS, DataType.DOUBLE, 1.0, 0.0, 10.0,
            "Multiplier for weather effects (wind, rain, fog) on hit chance. 0=disabled"),
    
    /** If headgear affects hit chance */
    FIREARM_HEAD_GEAR_EFFECT("FirearmHeadGearEffect", PropertyCategory.FIREARMS, DataType.BOOLEAN, false, null, null,
            "Enable to have headgear like welding masks affect hit chance"),
    
    // ===== NESTED: BASEMENT =====
    
    /** 
     * Basement spawn frequency.
     * 1=Never, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often, 7=Always
     */
    BASEMENT_SPAWN_FREQUENCY("Basement.SpawnFrequency", PropertyCategory.BASEMENT, DataType.INTEGER, 4, 1, 7,
            "How frequently basements spawn at random locations"),
    
    // ===== NESTED: MAP =====
    
    /** If mini-map window is available */
    MAP_ALLOW_MINI_MAP("Map.AllowMiniMap", PropertyCategory.MAP, DataType.BOOLEAN, false, null, null,
            "If enabled, a mini-map window will be available"),
    
    /** If world map can be accessed */
    MAP_ALLOW_WORLD_MAP("Map.AllowWorldMap", PropertyCategory.MAP, DataType.BOOLEAN, true, null, null,
            "If enabled, the world map can be accessed"),
    
    /** If world map starts completely filled in */
    MAP_ALL_KNOWN("Map.MapAllKnown", PropertyCategory.MAP, DataType.BOOLEAN, false, null, null,
            "If enabled, world map will be completely filled in on starting"),
    
    /** If maps need light to be read */
    MAP_NEEDS_LIGHT("Map.MapNeedsLight", PropertyCategory.MAP, DataType.BOOLEAN, true, null, null,
            "If enabled, maps can't be read unless there's a light source available"),
    
    // ===== NESTED: ZOMBIE LORE =====
    
    /** 
     * Zombie movement speed.
     * 1=Sprinters, 2=Fast Shamblers, 3=Shamblers, 4=Random
     */
    ZOMBIE_LORE_SPEED("ZombieLore.Speed", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 4, 1, 4,
            "How fast zombies move. 1=Sprinters, 2=Fast Shamblers, 3=Shamblers, 4=Random"),
    
    /** Percentage of zombies that are sprinters when speed is random. 0 to 100 */
    ZOMBIE_LORE_SPRINTER_PERCENTAGE("ZombieLore.SprinterPercentage", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 0, 0, 100,
            "If Random Speed enabled, percentage of zombies that are Sprinters"),
    
    /** 
     * Zombie damage per attack.
     * 1=Superhuman, 2=Normal, 3=Weak, 4=Random
     */
    ZOMBIE_LORE_STRENGTH("ZombieLore.Strength", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 2, 1, 4,
            "Damage zombies inflict per attack"),
    
    /** 
     * Zombie toughness.
     * 1=Tough, 2=Normal, 3=Fragile, 4=Random
     */
    ZOMBIE_LORE_TOUGHNESS("ZombieLore.Toughness", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 2, 1, 4,
            "Difficulty of killing a zombie"),
    
    /** 
     * Knox Virus transmission method.
     * 1=Blood and Saliva, 2=Saliva Only, 3=Everyone's Infected, 4=None
     */
    ZOMBIE_LORE_TRANSMISSION("ZombieLore.Transmission", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 1, 1, 4,
            "How the Knox Virus spreads"),
    
    /** 
     * Infection mortality speed.
     * 1=Instant, 2=0-30 Seconds, 3=0-1 Minutes, 4=0-12 Hours, 5=2-3 Days, 6=1-2 Weeks, 7=Never
     */
    ZOMBIE_LORE_MORTALITY("ZombieLore.Mortality", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 5, 1, 7,
            "How quickly the infection takes effect"),
    
    /** 
     * Corpse reanimation speed.
     * 1=Instant, 2=0-30 Seconds, 3=0-1 Minutes, 4=0-12 Hours, 5=2-3 Days, 6=1-2 Weeks
     */
    ZOMBIE_LORE_REANIMATE("ZombieLore.Reanimate", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 3, 1, 6,
            "How quickly infected corpses rise as zombies"),
    
    /** 
     * Zombie intelligence level.
     * 1=Navigate and Use Doors, 2=Navigate, 3=Basic Navigation, 4=Random
     */
    ZOMBIE_LORE_COGNITION("ZombieLore.Cognition", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 3, 1, 4,
            "Zombie intelligence"),
    
    /** 
     * How often zombies crawl under vehicles.
     * 1=Crawlers Only, 2=Extremely Rare, 3=Rare, 4=Sometimes, 5=Often, 6=Very Often, 7=Always
     */
    ZOMBIE_LORE_CRAWL_UNDER_VEHICLE("ZombieLore.CrawlUnderVehicle", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 5, 1, 7,
            "How often zombies can crawl under parked vehicles"),
    
    /** 
     * Zombie memory duration.
     * 1=Long, 2=Normal, 3=Short, 4=None, 5=Random, 6=Random between Normal and None
     */
    ZOMBIE_LORE_MEMORY("ZombieLore.Memory", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 2, 1, 6,
            "How long zombies remember player after seeing or hearing them"),
    
    /** 
     * Zombie vision radius.
     * 1=Eagle, 2=Normal, 3=Poor, 4=Random, 5=Random between Normal and Poor
     */
    ZOMBIE_LORE_SIGHT("ZombieLore.Sight", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 2, 1, 5,
            "Zombie vision radius"),
    
    /** 
     * Zombie hearing radius.
     * 1=Pinpoint, 2=Normal, 3=Poor, 4=Random, 5=Random between Normal and Poor
     */
    ZOMBIE_LORE_HEARING("ZombieLore.Hearing", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 2, 1, 5,
            "Zombie hearing radius"),
    
    /** If advanced stealth mechanics are active */
    ZOMBIE_LORE_SPOTTED_LOGIC("ZombieLore.SpottedLogic", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, true, null, null,
            "Activates advanced stealth mechanics: hide behind cars, traits and weather matter"),
    
    /** If zombies attack doors without chasing */
    ZOMBIE_LORE_THUMP_NO_CHASING("ZombieLore.ThumpNoChasing", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, false, null, null,
            "If zombies that haven't seen/heard player can attack doors while roaming"),
    
    /** If zombies can destroy player constructions */
    ZOMBIE_LORE_THUMP_ON_CONSTRUCTION("ZombieLore.ThumpOnConstruction", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, true, null, null,
            "If zombies can destroy player constructions and defenses"),
    
    /** 
     * When zombies are more active.
     * 1=Both, 2=Night, 3=Day
     */
    ZOMBIE_LORE_ACTIVE_ONLY("ZombieLore.ActiveOnly", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 1, 1, 3,
            "When zombies are more active. Active zombies use Speed setting, inactive are slower"),
    
    /** If zombies trigger house alarms */
    ZOMBIE_LORE_TRIGGER_HOUSE_ALARM("ZombieLore.TriggerHouseAlarm", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, false, null, null,
            "If zombies trigger house alarms when breaking through windows or doors"),
    
    /** If zombies can drag you down */
    ZOMBIE_LORE_DRAG_DOWN("ZombieLore.ZombiesDragDown", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, true, null, null,
            "If multiple attacking zombies can drag you down and kill you"),
    
    /** If crawlers contribute to drag down */
    ZOMBIE_LORE_CRAWLERS_DRAG_DOWN("ZombieLore.ZombiesCrawlersDragDown", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, false, null, null,
            "If crawler zombies beside player contribute to being dragged down"),
    
    /** If zombies lunge after climbing */
    ZOMBIE_LORE_FENCE_LUNGE("ZombieLore.ZombiesFenceLunge", PropertyCategory.ZOMBIE_LORE, DataType.BOOLEAN, true, null, null,
            "If zombies can lunge after climbing fence/window if you're too close"),
    
    /** Zombie armor effectiveness multiplier. 0.00 to 100.00 */
    ZOMBIE_LORE_ARMOR_FACTOR("ZombieLore.ZombiesArmorFactor", PropertyCategory.ZOMBIE_LORE, DataType.DOUBLE, 2.0, 0.0, 100.0,
            "Multiplier for effectiveness of armor worn by zombies"),
    
    /** Maximum zombie armor defense percentage. 0 to 100 */
    ZOMBIE_LORE_MAX_DEFENSE("ZombieLore.ZombiesMaxDefense", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 85, 0, 100,
            "Maximum defense percentage protective garments can provide to zombie"),
    
    /** Chance of zombie having attached weapon. 0 to 100 */
    ZOMBIE_LORE_ATTACHED_WEAPON_CHANCE("ZombieLore.ChanceOfAttachedWeapon", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 6, 0, 100,
            "Percentage chance of having random attached weapon"),
    
    /** Zombie fall damage multiplier. 0.00 to 100.00 */
    ZOMBIE_LORE_FALL_DAMAGE("ZombieLore.ZombiesFallDamage", PropertyCategory.ZOMBIE_LORE, DataType.DOUBLE, 1.0, 0.0, 100.0,
            "How much damage zombies take when falling from height"),
    
    /** 
     * Fake dead zombie settings.
     * 1=World Zombies, 2=World and Combat Zombies, 3=Never
     */
    ZOMBIE_LORE_DISABLE_FAKE_DEAD("ZombieLore.DisableFakeDead", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 1, 1, 3,
            "Whether some dead-looking zombies will reanimate and attack"),
    
    /** 
     * Zombie removal at player spawn.
     * 1=Inside building and around it, 2=Inside building, 3=Inside room, 4=Zombies can spawn anywhere
     */
    ZOMBIE_LORE_PLAYER_SPAWN_REMOVAL("ZombieLore.PlayerSpawnZombieRemoval", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 1, 1, 4,
            "Zombies won't spawn where players spawn"),
    
    /** Zombies needed to damage tall fence. -1 to 100 */
    ZOMBIE_LORE_FENCE_THUMPERS_REQUIRED("ZombieLore.FenceThumpersRequired", PropertyCategory.ZOMBIE_LORE, DataType.INTEGER, 50, -1, 100,
            "How many zombies it takes to damage a tall fence"),
    
    /** Fence damage speed multiplier. 0.01 to 100.00 */
    ZOMBIE_LORE_FENCE_DAMAGE_MULTIPLIER("ZombieLore.FenceDamageMultiplier", PropertyCategory.ZOMBIE_LORE, DataType.DOUBLE, 1.0, 0.01, 100.0,
            "How quickly zombies damage tall fences"),
    
    // ===== NESTED: ZOMBIE CONFIG =====
    
    /** Zombie population multiplier. 0.00 to 4.00 */
    ZOMBIE_CONFIG_POPULATION_MULTIPLIER("ZombieConfig.PopulationMultiplier", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 0.65, 0.0, 4.0,
            "Set by Zombie Count option. Insane=2.5, Very High=1.6, High=1.2, Normal=0.65, Low=0.15, None=0.0"),
    
    /** Starting population multiplier. 0.00 to 4.00 */
    ZOMBIE_CONFIG_POPULATION_START_MULTIPLIER("ZombieConfig.PopulationStartMultiplier", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 1.0, 0.0, 4.0,
            "Multiplier for desired zombie population at game start"),
    
    /** Peak population multiplier. 0.00 to 4.00 */
    ZOMBIE_CONFIG_POPULATION_PEAK_MULTIPLIER("ZombieConfig.PopulationPeakMultiplier", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 1.5, 0.0, 4.0,
            "Multiplier for desired zombie population on peak day"),
    
    /** Day when population peaks. 1 to 365 */
    ZOMBIE_CONFIG_POPULATION_PEAK_DAY("ZombieConfig.PopulationPeakDay", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 28, 1, 365,
            "Day when the population reaches its peak"),
    
    /** Hours before zombies can respawn. 0.00 to 8760.00 */
    ZOMBIE_CONFIG_RESPAWN_HOURS("ZombieConfig.RespawnHours", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 72.0, 0.0, 8760.0,
            "Hours that must pass before zombies may respawn in cell. 0=disabled"),
    
    /** Hours chunk must be unseen before respawn. 0.00 to 8760.00 */
    ZOMBIE_CONFIG_RESPAWN_UNSEEN_HOURS("ZombieConfig.RespawnUnseenHours", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 16.0, 0.0, 8760.0,
            "Hours chunk must be unseen before zombies may respawn in it"),
    
    /** Respawn population fraction. 0.00 to 1.00 */
    ZOMBIE_CONFIG_RESPAWN_MULTIPLIER("ZombieConfig.RespawnMultiplier", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 0.1, 0.0, 1.0,
            "Fraction of cell's desired population that may respawn every RespawnHours"),
    
    /** Hours before zombie redistribution. 0.00 to 8760.00 */
    ZOMBIE_CONFIG_REDISTRIBUTE_HOURS("ZombieConfig.RedistributeHours", PropertyCategory.ZOMBIE_CONFIG, DataType.DOUBLE, 12.0, 0.0, 8760.0,
            "Hours that must pass before zombies migrate to empty parts of same cell. 0=disabled"),
    
    /** Distance zombies walk toward sound. 10 to 1000 */
    ZOMBIE_CONFIG_FOLLOW_SOUND_DISTANCE("ZombieConfig.FollowSoundDistance", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 100, 10, 1000,
            "Distance zombie will try to walk towards last sound it heard"),
    
    /** Size of idle zombie groups. 0 to 1000 */
    ZOMBIE_CONFIG_RALLY_GROUP_SIZE("ZombieConfig.RallyGroupSize", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 20, 0, 1000,
            "Size of groups real zombies form when idle. 0=no groups. Groups don't form in buildings/forests"),
    
    /** Rally group size variance percentage. 0 to 100 */
    ZOMBIE_CONFIG_RALLY_GROUP_SIZE_VARIANCE("ZombieConfig.RallyGroupSizeVariance", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 50, 0, 100,
            "Percentage that zombie groups can vary from default size (both larger and smaller)"),
    
    /** Distance zombies travel to form groups. 5 to 50 */
    ZOMBIE_CONFIG_RALLY_TRAVEL_DISTANCE("ZombieConfig.RallyTravelDistance", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 20, 5, 50,
            "Distance real zombies travel to form groups when idle"),
    
    /** Distance between zombie groups. 5 to 25 */
    ZOMBIE_CONFIG_RALLY_GROUP_SEPARATION("ZombieConfig.RallyGroupSeparation", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 15, 5, 25,
            "Distance between zombie groups"),
    
    /** Rally group member radius. 1 to 10 */
    ZOMBIE_CONFIG_RALLY_GROUP_RADIUS("ZombieConfig.RallyGroupRadius", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 3, 1, 10,
            "How close members of zombie group stay to group's leader"),
    
    /** Zombie count before deletion. 10 to 500 */
    ZOMBIE_CONFIG_ZOMBIES_COUNT_BEFORE_DELETE("ZombieConfig.ZombiesCountBeforeDelete", PropertyCategory.ZOMBIE_CONFIG, DataType.INTEGER, 300, 10, 500,
            "Maximum zombie count before deletion"),
    
    // ===== NESTED: MULTIPLIER CONFIG (SKILLS) =====
    
    /** Global skill XP multiplier. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_GLOBAL("MultiplierConfig.Global", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which all skills level up"),
    
    /** If all skills use global multiplier */
    MULTIPLIER_CONFIG_GLOBAL_TOGGLE("MultiplierConfig.GlobalToggle", PropertyCategory.MULTIPLIER_CONFIG, DataType.BOOLEAN, false, null, null,
            "When enabled, all skills will use the Global Multiplier"),
    
    /** Fitness skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_FITNESS("MultiplierConfig.Fitness", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Fitness skill levels up"),
    
    /** Strength skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_STRENGTH("MultiplierConfig.Strength", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Strength skill levels up"),
    
    /** Sprinting skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_SPRINTING("MultiplierConfig.Sprinting", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Sprinting skill levels up"),
    
    /** Lightfooted skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_LIGHTFOOT("MultiplierConfig.Lightfoot", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Lightfooted skill levels up"),
    
    /** Nimble skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_NIMBLE("MultiplierConfig.Nimble", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Nimble skill levels up"),
    
    /** Sneaking skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_SNEAK("MultiplierConfig.Sneak", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Sneaking skill levels up"),
    
    /** Axe skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_AXE("MultiplierConfig.Axe", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Axe skill levels up"),
    
    /** Long Blunt skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_BLUNT("MultiplierConfig.Blunt", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Long Blunt skill levels up"),
    
    /** Short Blunt skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_SMALL_BLUNT("MultiplierConfig.SmallBlunt", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Short Blunt skill levels up"),
    
    /** Long Blade skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_LONG_BLADE("MultiplierConfig.LongBlade", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Long Blade skill levels up"),
    
    /** Short Blade skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_SMALL_BLADE("MultiplierConfig.SmallBlade", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Short Blade skill levels up"),
    
    /** Spear skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_SPEAR("MultiplierConfig.Spear", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Spear skill levels up"),
    
    /** Maintenance skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_MAINTENANCE("MultiplierConfig.Maintenance", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Maintenance skill levels up"),
    
    /** Carpentry skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_WOODWORK("MultiplierConfig.Woodwork", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Carpentry skill levels up"),
    
    /** Cooking skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_COOKING("MultiplierConfig.Cooking", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Cooking skill levels up"),
    
    /** Agriculture skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_FARMING("MultiplierConfig.Farming", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Agriculture skill levels up"),
    
    /** First Aid skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_DOCTOR("MultiplierConfig.Doctor", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which First Aid skill levels up"),
    
    /** Electrical skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_ELECTRICITY("MultiplierConfig.Electricity", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Electrical skill levels up"),
    
    /** Welding skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_METAL_WELDING("MultiplierConfig.MetalWelding", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Welding skill levels up"),
    
    /** Mechanics skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_MECHANICS("MultiplierConfig.Mechanics", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Mechanics skill levels up"),
    
    /** Tailoring skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_TAILORING("MultiplierConfig.Tailoring", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Tailoring skill levels up"),
    
    /** Aiming skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_AIMING("MultiplierConfig.Aiming", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Aiming skill levels up"),
    
    /** Reloading skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_RELOADING("MultiplierConfig.Reloading", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Reloading skill levels up"),
    
    /** Fishing skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_FISHING("MultiplierConfig.Fishing", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Fishing skill levels up"),
    
    /** Trapping skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_TRAPPING("MultiplierConfig.Trapping", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Trapping skill levels up"),
    
    /** Foraging skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_PLANT_SCAVENGING("MultiplierConfig.PlantScavenging", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Foraging skill levels up"),
    
    /** Knapping skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_FLINT_KNAPPING("MultiplierConfig.FlintKnapping", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Knapping skill levels up"),
    
    /** Masonry skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_MASONRY("MultiplierConfig.Masonry", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Masonry skill levels up"),
    
    /** Pottery skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_POTTERY("MultiplierConfig.Pottery", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Pottery skill levels up"),
    
    /** Carving skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_CARVING("MultiplierConfig.Carving", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Carving skill levels up"),
    
    /** Animal Care skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_HUSBANDRY("MultiplierConfig.Husbandry", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Animal Care skill levels up"),
    
    /** Tracking skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_TRACKING("MultiplierConfig.Tracking", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Tracking skill levels up"),
    
    /** Blacksmithing skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_BLACKSMITH("MultiplierConfig.Blacksmith", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Blacksmithing skill levels up"),
    
    /** Butchering skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_BUTCHERING("MultiplierConfig.Butchering", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Butchering skill levels up"),
    
    /** Glassmaking skill XP rate. 0.00 to 1000.00 */
    MULTIPLIER_CONFIG_GLASSMAKING("MultiplierConfig.Glassmaking", PropertyCategory.MULTIPLIER_CONFIG, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Rate at which Glassmaking skill levels up"),
    
    // ===== NESTED: MOD SPECIFIC =====
    
    /** Vehicle claim system - max claims per player. 1 to 20 */
    VEHICLE_CLAIM_SYSTEM_MAX_CLAIMS("VehicleClaimSystem.MaxClaimsPerPlayer", PropertyCategory.MOD_VEHICLE_CLAIM, DataType.INTEGER, 3, 1, 20,
            "Maximum vehicle claims per player"),
    
    /** Skill recovery journal - recovery percentage. 1 to 100 */
    SKILL_RECOVERY_RECOVERY_PERCENTAGE("SkillRecoveryJournal.RecoveryPercentage", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, 100, 1, 100,
            "Skill recovery percentage from journal"),
    
    /** Skill recovery journal - transcribe speed. 0.00 to 1000.00 */
    SKILL_RECOVERY_TRANSCRIBE_SPEED("SkillRecoveryJournal.TranscribeSpeed", PropertyCategory.MOD_SKILL_RECOVERY, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Speed multiplier for transcribing to journal"),
    
    /** Skill recovery journal - read time speed. 0.00 to 1000.00 */
    SKILL_RECOVERY_READ_TIME_SPEED("SkillRecoveryJournal.ReadTimeSpeed", PropertyCategory.MOD_SKILL_RECOVERY, DataType.DOUBLE, 1.0, 0.0, 1000.0,
            "Speed multiplier for reading journal"),
    
    /** Skill recovery journal - recover profession/traits bonuses */
    SKILL_RECOVERY_RECOVER_PROFESSION_TRAITS("SkillRecoveryJournal.RecoverProfessionAndTraitsBonuses", PropertyCategory.MOD_SKILL_RECOVERY, DataType.BOOLEAN, false, null, null,
            "If profession and traits bonuses are recovered"),
    
    /** Skill recovery journal - transcribe TV XP */
    SKILL_RECOVERY_TRANSCRIBE_TV_XP("SkillRecoveryJournal.TranscribeTVXP", PropertyCategory.MOD_SKILL_RECOVERY, DataType.BOOLEAN, false, null, null,
            "If TV-learned XP can be transcribed"),
    
    /** Skill recovery journal - recover passive skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_PASSIVE_SKILLS("SkillRecoveryJournal.RecoverPassiveSkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, 0, -1, 100,
            "Recover passive skills level. -1=disabled"),
    
    /** Skill recovery journal - recover physical category skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_PHYSICAL_SKILLS("SkillRecoveryJournal.RecoverPhysicalCategorySkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, -1, -1, 100,
            "Recover physical category skills level. -1=disabled"),
    
    /** Skill recovery journal - recover combat skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_COMBAT_SKILLS("SkillRecoveryJournal.RecoverCombatSkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, -1, -1, 100,
            "Recover combat skills level. -1=disabled"),
    
    /** Skill recovery journal - recover firearm skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_FIREARM_SKILLS("SkillRecoveryJournal.RecoverFirearmSkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, -1, -1, 100,
            "Recover firearm skills level. -1=disabled"),
    
    /** Skill recovery journal - recover crafting skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_CRAFTING_SKILLS("SkillRecoveryJournal.RecoverCraftingSkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, -1, -1, 100,
            "Recover crafting skills level. -1=disabled"),
    
    /** Skill recovery journal - recover survivalist skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_SURVIVALIST_SKILLS("SkillRecoveryJournal.RecoverSurvivalistSkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, -1, -1, 100,
            "Recover survivalist skills level. -1=disabled"),
    
    /** Skill recovery journal - recover farming category skills. -1 to 100 */
    SKILL_RECOVERY_RECOVER_FARMING_SKILLS("SkillRecoveryJournal.RecoverFarmingCategorySkills", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, -1, -1, 100,
            "Recover farming category skills level. -1=disabled"),
    
    /** Skill recovery journal - recover recipes */
    SKILL_RECOVERY_RECOVER_RECIPES("SkillRecoveryJournal.RecoverRecipes", PropertyCategory.MOD_SKILL_RECOVERY, DataType.BOOLEAN, true, null, null,
            "If recipes are recovered"),
    
    /** Skill recovery journal - journal has been used */
    SKILL_RECOVERY_JOURNAL_USED("SkillRecoveryJournal.RecoveryJournalUsed", PropertyCategory.MOD_SKILL_RECOVERY, DataType.BOOLEAN, false, null, null,
            "If recovery journal has been used"),
    
    /** Skill recovery journal - kills track. 0 to 100 */
    SKILL_RECOVERY_KILLS_TRACK("SkillRecoveryJournal.KillsTrack", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, 0, 0, 100,
            "Zombie kills tracking level"),
    
    /** Skill recovery journal - craft recipe */
    SKILL_RECOVERY_CRAFT_RECIPE("SkillRecoveryJournal.CraftRecipe", PropertyCategory.MOD_SKILL_RECOVERY, DataType.STRING, "", null, null,
            "Recipe required to craft journal"),
    
    /** Skill recovery journal - craft recipe need learn */
    SKILL_RECOVERY_CRAFT_RECIPE_NEED_LEARN("SkillRecoveryJournal.CraftRecipeNeedLearn", PropertyCategory.MOD_SKILL_RECOVERY, DataType.BOOLEAN, false, null, null,
            "If craft recipe must be learned first"),
    
    /** Skill recovery journal - mod data track */
    SKILL_RECOVERY_MOD_DATA_TRACK("SkillRecoveryJournal.ModDataTrack", PropertyCategory.MOD_SKILL_RECOVERY, DataType.STRING, "", null, null,
            "Mod data tracking string"),
    
    /** Skill recovery journal - security features. Integer value */
    SKILL_RECOVERY_SECURITY_FEATURES("SkillRecoveryJournal.SecurityFeatures", PropertyCategory.MOD_SKILL_RECOVERY, DataType.INTEGER, 1, 0, 10,
            "Security features level"),
    
    /** Starting injuries mod - enable blackout explosions */
    STARTING_INJURIES_BLACKOUT_EXPLOSIONS("StartingInjuriesMod.EnableBlackoutExplosions", PropertyCategory.MOD_STARTING_INJURIES, DataType.BOOLEAN, true, null, null,
            "If blackout explosions are enabled"),
    
    /** Starting injuries mod - enable bandage assistance */
    STARTING_INJURIES_BANDAGE_ASSISTANCE("StartingInjuriesMod.EnableBandageAssistance", PropertyCategory.MOD_STARTING_INJURIES, DataType.BOOLEAN, false, null, null,
            "If bandage assistance is enabled"),
    
    /** They Knew Screamer - time mode. 0 to 2 */
    TSY_TIME_MODE("SIMBAproduz_TSY.TimeMode", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 0, 0, 2,
            "Time mode setting"),
    
    /** They Knew Screamer - screech chance. 0 to 100 */
    TSY_SCREECH_CHANCE("SIMBAproduz_TSY.ScreechChance", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 60, 0, 100,
            "General screech chance percentage"),
    
    /** They Knew Screamer - chase screech chance. 0 to 100 */
    TSY_CHASE_SCREECH_CHANCE("SIMBAproduz_TSY.ChaseScreechChance", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 50, 0, 100,
            "Chase screech chance percentage"),
    
    /** They Knew Screamer - chase cooldown hours. 0.00 to 0.10 */
    TSY_CHASE_COOLDOWN_HOURS("SIMBAproduz_TSY.ChaseCooldownHours", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.DOUBLE, 0.03, 0.0, 0.1,
            "Hours cooldown between chase screeches"),
    
    /** They Knew Screamer - global volume. 0.10 to 3.00 */
    TSY_GLOBAL_VOLUME("SIMBAproduz_TSY.GlobalVolume", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.DOUBLE, 1.0, 0.1, 3.0,
            "Global volume multiplier"),
    
    /** They Knew Screamer - far volume. 0.01 to 1.00 */
    TSY_FAR_VOLUME("SIMBAproduz_TSY.FarVolume", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.DOUBLE, 0.08, 0.01, 1.0,
            "Volume for far screeches"),
    
    /** They Knew Screamer - near volume. 0.10 to 3.00 */
    TSY_NEAR_VOLUME("SIMBAproduz_TSY.NearVolume", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.DOUBLE, 1.0, 0.1, 3.0,
            "Volume for near screeches"),
    
    /** They Knew Screamer - far range. 10 to 300 */
    TSY_FAR_RANGE("SIMBAproduz_TSY.FarRange", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 150, 10, 300,
            "Range for far screeches"),
    
    /** They Knew Screamer - max process distance. 50 to 400 */
    TSY_MAX_PROCESS_DISTANCE("SIMBAproduz_TSY.MaxProcessDistance", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 220, 50, 400,
            "Maximum processing distance"),
    
    /** They Knew Screamer - tick rate. 1 to 20 */
    TSY_TICK_RATE("SIMBAproduz_TSY.TickRate", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 6, 1, 20,
            "Processing tick rate"),
    
    /** They Knew Screamer - alert nearby zombies */
    TSY_ALERT_NEARBY_ZOMBIES("SIMBAproduz_TSY.AlertNearbyZombies", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.BOOLEAN, true, null, null,
            "If screeches alert nearby zombies"),
    
    /** They Knew Screamer - alert radius. 10 to 120 */
    TSY_ALERT_RADIUS("SIMBAproduz_TSY.AlertRadius", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 80, 10, 120,
            "Radius for zombie alerting"),
    
    /** They Knew Screamer - cluster radius. 5 to 80 */
    TSY_CLUSTER_RADIUS("SIMBAproduz_TSY.ClusterRadius", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 35, 5, 80,
            "Radius for screamer clustering"),
    
    /** They Knew Screamer - cluster cooldown hours. 0.00 to 0.10 */
    TSY_CLUSTER_COOLDOWN_HOURS("SIMBAproduz_TSY.ClusterCooldownHours", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.DOUBLE, 0.01, 0.0, 0.1,
            "Hours cooldown between cluster screeches"),
    
    /** They Knew Screamer - max cluster screams. 1 to 10 */
    TSY_MAX_CLUSTER_SCREAMS("SIMBAproduz_TSY.MaxClusterScreams", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.INTEGER, 1, 1, 10,
            "Maximum screeches in a cluster"),
    
    /** They Knew Screamer - enable near scream */
    TSY_ENABLE_NEAR_SCREAM("SIMBAproduz_TSY.EnableNearScream", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.BOOLEAN, true, null, null,
            "If near screeches are enabled"),
    
    /** They Knew Screamer - near as world sound */
    TSY_NEAR_AS_WORLD_SOUND("SIMBAproduz_TSY.NearAsWorldSound", PropertyCategory.MOD_THEY_KNEW_SCREAMER, DataType.BOOLEAN, false, null, null,
            "If near screeches are treated as world sounds");
    
    // ===== ENUM FIELDS =====
    
    private final String key;
    private final PropertyCategory category;
    private final DataType dataType;
    private final Object defaultValue;
    private final Number minValue;
    private final Number maxValue;
    private final String description;
    
    // ===== CONSTRUCTOR =====
    
    SandboxProperty(String key, PropertyCategory category, DataType dataType, 
                   Object defaultValue, Number minValue, Number maxValue, String description) {
        this.key = key;
        this.category = category;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.description = description;
    }
    
    // ===== GETTERS =====
    
    /** Get the Lua key for this property */
    public String getKey() {
        return key;
    }
    
    /** Get the category this property belongs to */
    public PropertyCategory getCategory() {
        return category;
    }
    
    /** Get the data type of this property */
    public DataType getDataType() {
        return dataType;
    }
    
    /** Get the default value */
    public Object getDefaultValue() {
        return defaultValue;
    }
    
    /** Get minimum value (if applicable) */
    public Optional<Number> getMinValue() {
        return Optional.ofNullable(minValue);
    }
    
    /** Get maximum value (if applicable) */
    public Optional<Number> getMaxValue() {
        return Optional.ofNullable(maxValue);
    }
    
    /** Get property description */
    public String getDescription() {
        return description;
    }
    
    /** Check if this property is nested (has a dot in key) */
    public boolean isNested() {
        return key.contains(".");
    }
    
    /** Get the parent key if nested (e.g., "ZombieLore" from "ZombieLore.Speed") */
    public Optional<String> getParentKey() {
        if (isNested()) {
            return Optional.of(key.substring(0, key.lastIndexOf('.')));
        }
        return Optional.empty();
    }
    
    /** Get the leaf key if nested (e.g., "Speed" from "ZombieLore.Speed") */
    public String getLeafKey() {
        if (isNested()) {
            return key.substring(key.lastIndexOf('.') + 1);
        }
        return key;
    }
    
    // ===== UTILITY METHODS =====
    
    /** Find property by Lua key */
    public static Optional<SandboxProperty> fromKey(String key) {
        return Arrays.stream(values())
                .filter(p -> p.key.equals(key))
                .findFirst();
    }
    
    /** Get all properties in a category */
    public static SandboxProperty[] getByCategory(PropertyCategory category) {
        return Arrays.stream(values())
                .filter(p -> p.category == category)
                .toArray(SandboxProperty[]::new);
    }
    
    /** Validate a value against this property's constraints */
    public boolean isValidValue(Object value) {
        if (value == null) return false;
        
        switch (dataType) {
            case BOOLEAN:
                return value instanceof Boolean;
                
            case INTEGER:
                if (!(value instanceof Number)) return false;
                int intVal = ((Number) value).intValue();
                if (minValue != null && intVal < minValue.intValue()) return false;
                if (maxValue != null && intVal > maxValue.intValue()) return false;
                return true;
                
            case DOUBLE:
                if (!(value instanceof Number)) return false;
                double doubleVal = ((Number) value).doubleValue();
                if (minValue != null && doubleVal < minValue.doubleValue()) return false;
                if (maxValue != null && doubleVal > maxValue.doubleValue()) return false;
                return true;
                
            case STRING:
                return value instanceof String;
                
            default:
                return false;
        }
    }
    
    // ===== NESTED ENUMS =====
    
    /** Property categories for organization */
    public enum PropertyCategory {
        CORE("Core Settings"),
        ZOMBIE("Zombie Settings"),
        TIME("Time & Date"),
        WEATHER("Weather & Climate"),
        UTILITIES("Utilities & Services"),
        LOOT("Loot Settings"),
        WORLD("World & Environment"),
        PLAYER("Player & Survival"),
        EVENTS("Events & Meta"),
        GENERATOR("Generators"),
        CONSTRUCTION("Construction & Structures"),
        NIGHT("Night & Lighting"),
        CORPSES("Corpses & Gore"),
        FIRE("Fire"),
        VEHICLE("Vehicles"),
        COMBAT("Combat"),
        ZOMBIE_LORE("Zombie Lore"),
        ANIMALS("Animals"),
        VERMIN("Vermin"),
        FISHING("Fishing"),
        FARMING("Farming"),
        CRAFTING("Crafting"),
        SKILLS("Skills & XP"),
        FIREARMS("Firearms"),
        BASEMENT("Basement Settings"),
        MAP("Map Settings"),
        ZOMBIE_CONFIG("Zombie Configuration"),
        MULTIPLIER_CONFIG("Skill Multipliers"),
        MOD_VEHICLE_CLAIM("Mod: Vehicle Claim System"),
        MOD_SKILL_RECOVERY("Mod: Skill Recovery Journal"),
        MOD_STARTING_INJURIES("Mod: Starting Injuries"),
        MOD_THEY_KNEW_SCREAMER("Mod: They Knew (Screamer)");
        
        private final String displayName;
        
        PropertyCategory(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /** Data type enum */
    public enum DataType {
        BOOLEAN(Boolean.class),
        INTEGER(Integer.class),
        DOUBLE(Double.class),
        STRING(String.class);
        
        private final Class<?> javaType;
        
        DataType(Class<?> javaType) {
            this.javaType = javaType;
        }
        
        public Class<?> getJavaType() {
            return javaType;
        }
        
        /** Parse string value to appropriate type */
        public Object parse(String value) {
            switch (this) {
                case BOOLEAN:
                    return Boolean.parseBoolean(value);
                case INTEGER:
                    return Integer.parseInt(value);
                case DOUBLE:
                    return Double.parseDouble(value);
                case STRING:
                    return value;
                default:
                    throw new IllegalArgumentException("Unknown data type: " + this);
            }
        }
        
        /** Format value for Lua output */
        public String formatForLua(Object value) {
            switch (this) {
                case BOOLEAN:
                    return value.toString();
                case INTEGER:
                case DOUBLE:
                    return value.toString();
                case STRING:
                    return "\"" + value.toString().replace("\"", "\\\"") + "\"";
                default:
                    throw new IllegalArgumentException("Unknown data type: " + this);
            }
        }
    }
}
