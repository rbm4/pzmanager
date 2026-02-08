package com.apocalipsebr.zomboid.server.manager.presentation.dto;

import com.apocalipsebr.zomboid.server.manager.application.constants.SandboxProperty;
import java.util.Optional;

/**
 * Data Transfer Object for Sandbox Properties
 */
public class SandboxPropertyDTO {
    private String key;
    private String category;
    private String dataType;
    private Object currentValue;
    private Object defaultValue;
    private Number minValue;
    private Number maxValue;
    private String description;

    public SandboxPropertyDTO() {}

    public SandboxPropertyDTO(SandboxProperty property, Object currentValue) {
        this.key = property.getKey();
        this.category = property.getCategory().getDisplayName();
        this.dataType = property.getDataType().toString();
        this.currentValue = currentValue;
        this.defaultValue = property.getDefaultValue();
        this.minValue = property.getMinValue().orElse(null);
        this.maxValue = property.getMaxValue().orElse(null);
        this.description = property.getDescription();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Object getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Object currentValue) {
        this.currentValue = currentValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Number getMinValue() {
        return minValue;
    }

    public void setMinValue(Number minValue) {
        this.minValue = minValue;
    }

    public Number getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Number maxValue) {
        this.maxValue = maxValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
