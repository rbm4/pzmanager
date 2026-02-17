package com.apocalipsebr.zomboid.server.manager.application.constants;

public enum RegionProperty {
    SPRINTER_CHANCE("sprinterChance","0"),
    PVP_ENABLED("pvpEnabled","false"),
    HAWK_VISION_CHANCE("hawkVisionChance","0"),
    BAD_VISION_CHANCE("badVisionChance","0"),
    GOOD_HEARING_CHANCE("goodHearingChance","0"),
    BAD_HEARING_CHANCE("badHearingChance","0"),
    ZOMBIE_ARMOR_FACTOR("zombieArmorFactor","85"),
    RESISTANT_CHANCE("resistantChance","0"),
    SHAMBLER_CHANCE("shamblerChance","0"),
    NORMAL_VISION_CHANCE("normalVisionChance","0"),
    POOR_VISION_CHANCE("poorVisionChance","0"),
    RANDOM_VISION_CHANCE("randomVisionChance","0"),
    PINPOINT_HEARING_CHANCE("pinpointHearingChance","0"),
    NORMAL_HEARING_CHANCE("normalHearingChance","0"),
    POOR_HEARING_CHANCE("poorHearingChance","0"),
    RANDOM_HEARING_CHANCE("randomHearingChance","0"),
    RANDOM_NORMAL_POOR_HEARING_CHANCE("randomNormalPoorHearingChance","0"),
    TOUGHNESS_CHANCE("toughnessChance","0"),
    NORMAL_TOUGHNESS_CHANCE("normalToughnessChance","0"),
    FRAGILE_CHANCE("fragileChance","0"),
    RANDOM_TOUGHNESS_CHANCE("randomToughnessChance","0"),
    SUPERHUMAN_CHANCE("superhumanChance","0"),
    NORMAL_TOUGHNESS("normalToughness","0"),
    WEAK_CHANCE("weakChance","0"),
    RANDOM_TOUGHNESS("randomToughness","0"),
    NAVIGATION_CHANCE("navigationChance","0"),
    NAVIGATION_LONG_CHANCE("navigationLongChance","0"),
    NAVIGATION_NORMAL_CHANCE("navigationNormalChance","0"),
    NAVIGATION_SHORT_CHANCE("navigationShortChance","0"),
    NAVIGATION_NONE_CHANCE("navigationNoneChance","0"),
    NAVIGATION_RANDOM_CHANCE("navigationRandomChance","0"),
    MEMORY_LONG_CHANCE("memoryLongChance","0"),
    MEMORY_NORMAL_CHANCE("memoryNormalChance","0"),
    MEMORY_SHORT_CHANCE("memoryShortChance","0"),
    MEMORY_NONE_CHANCE("memoryNoneChance","0"),
    MEMORY_RANDOM_CHANCE("memoryRandomChance","0"),
    ARMOR_EFFECTIVENESS_MULTIPLIER("armorEffectivenessMultiplier","0"),
    ARMOR_DEFENSE_PERCENTAGE("armorDefensePercentage","0"),
    MESSAGE("message","Voce acabou de entrar em uma zona XXX!");

    private RegionPropetyRecord properties;

    RegionProperty(String name, String suggestedValue) {
        this.properties = new RegionPropetyRecord(name,suggestedValue);
    }
    
    public RegionPropetyRecord getProperties(){
        return this.properties;
    }
}

record RegionPropetyRecord(String name, String suggestedValue){}
