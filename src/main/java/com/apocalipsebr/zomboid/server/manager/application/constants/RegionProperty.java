package com.apocalipsebr.zomboid.server.manager.application.constants;

public enum RegionProperty {
    SPRINTER_CHANCE("sprinterChance","0"),PVP_ENABLED("pvpEnabled","false"),MESSAGE("message","Voce acabou de entrar em uma zona XXX!");

    private RegionPropetyRecord properties;

    RegionProperty(String name, String suggestedValue) {
        this.properties = new RegionPropetyRecord(name,suggestedValue);
    }
    
    public RegionPropetyRecord getProperties(){
        return this.properties;
    }
}

record RegionPropetyRecord(String name, String suggestedValue){}
