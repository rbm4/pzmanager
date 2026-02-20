package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import com.apocalipsebr.zomboid.server.manager.application.constants.EventPropertySuggestion;

import jakarta.persistence.*;

@Entity
@Table(name = "game_event_properties")
public class GameEventProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_event_id", nullable = false)
    private GameEvent gameEvent;

    /** The EventPropertySuggestion enum name (e.g. "XP_BOOST", "SPRINTER_ZONE"). */
    @Column(name = "suggestion_key", nullable = false, length = 50)
    private String suggestionKey;

    /** SANDBOX or REGION */
    @Column(name = "property_target", nullable = false, length = 10)
    private String propertyTarget;

    /** Actual property key (e.g. "XPMultiplier", "sprinterChance"). */
    @Column(name = "property_key", nullable = false)
    private String propertyKey;

    /** User-friendly display name. */
    @Column(name = "display_name", nullable = false)
    private String displayName;

    /** PERCENTAGE, ABSOLUTE, or BOOLEAN */
    @Column(name = "value_type", nullable = false, length = 15)
    private String valueType;

    /** What the user selected (e.g. "30" for 30%, "true" for boolean). */
    @Column(name = "selected_value", nullable = false)
    private String selectedValue;

    /** The actual delta/value to apply to the property. */
    @Column(name = "calculated_delta")
    private String calculatedDelta;

    /** Cost of this individual property selection. */
    @Column(name = "property_cost", nullable = false)
    private Integer propertyCost;

    // Region-specific coordinate fields
    @Column(name = "region_x1")
    private Integer regionX1;

    @Column(name = "region_x2")
    private Integer regionX2;

    @Column(name = "region_y1")
    private Integer regionY1;

    @Column(name = "region_y2")
    private Integer regionY2;

    @Column(name = "region_z")
    private Integer regionZ;

    /** Reference to the Region entity created when the event is activated. */
    @Column(name = "linked_region_id")
    private Long linkedRegionId;

    public GameEventProperty() {}

    /**
     * Resolves the corresponding EventPropertySuggestion enum value.
     */
    public EventPropertySuggestion getSuggestion() {
        return EventPropertySuggestion.fromName(suggestionKey);
    }

    // ==================== Getters and Setters ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public GameEvent getGameEvent() { return gameEvent; }
    public void setGameEvent(GameEvent gameEvent) { this.gameEvent = gameEvent; }

    public String getSuggestionKey() { return suggestionKey; }
    public void setSuggestionKey(String suggestionKey) { this.suggestionKey = suggestionKey; }

    public String getPropertyTarget() { return propertyTarget; }
    public void setPropertyTarget(String propertyTarget) { this.propertyTarget = propertyTarget; }

    public String getPropertyKey() { return propertyKey; }
    public void setPropertyKey(String propertyKey) { this.propertyKey = propertyKey; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getValueType() { return valueType; }
    public void setValueType(String valueType) { this.valueType = valueType; }

    public String getSelectedValue() { return selectedValue; }
    public void setSelectedValue(String selectedValue) { this.selectedValue = selectedValue; }

    public String getCalculatedDelta() { return calculatedDelta; }
    public void setCalculatedDelta(String calculatedDelta) { this.calculatedDelta = calculatedDelta; }

    public Integer getPropertyCost() { return propertyCost; }
    public void setPropertyCost(Integer propertyCost) { this.propertyCost = propertyCost; }

    public Integer getRegionX1() { return regionX1; }
    public void setRegionX1(Integer regionX1) { this.regionX1 = regionX1; }

    public Integer getRegionX2() { return regionX2; }
    public void setRegionX2(Integer regionX2) { this.regionX2 = regionX2; }

    public Integer getRegionY1() { return regionY1; }
    public void setRegionY1(Integer regionY1) { this.regionY1 = regionY1; }

    public Integer getRegionY2() { return regionY2; }
    public void setRegionY2(Integer regionY2) { this.regionY2 = regionY2; }

    public Integer getRegionZ() { return regionZ; }
    public void setRegionZ(Integer regionZ) { this.regionZ = regionZ; }

    public Long getLinkedRegionId() { return linkedRegionId; }
    public void setLinkedRegionId(Long linkedRegionId) { this.linkedRegionId = linkedRegionId; }
}
