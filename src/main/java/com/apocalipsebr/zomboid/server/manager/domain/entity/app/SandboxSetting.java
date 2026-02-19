package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sandbox_settings", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"setting_key", "config_type"})
})
public class SandboxSetting {

    /**
     * Differentiates the source config file for this setting.
     */
    public enum ConfigType {
        /** From servertest.ini */
        SANDBOX,
        /** From servertest_SandboxVars.lua */
        SANDBOX_VARS,
        /** From servertest_spawnregions.lua */
        SPAWN_REGIONS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false)
    private String settingKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false, length = 20)
    private ConfigType configType = ConfigType.SANDBOX;

    @Column(name = "current_value", columnDefinition = "TEXT")
    private String currentValue;

    @Column(name = "applied_value", columnDefinition = "TEXT")
    private String appliedValue;

    @Column(name = "overwrite_at_startup", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean overwriteAtStartup = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SandboxSetting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.overwriteAtStartup = false;
        this.configType = ConfigType.SANDBOX;
    }

    public SandboxSetting(String settingKey, String currentValue, String description) {
        this();
        this.settingKey = settingKey;
        this.currentValue = currentValue;
        this.description = description;
    }

    public SandboxSetting(String settingKey, String currentValue, String description, ConfigType configType) {
        this();
        this.settingKey = settingKey;
        this.currentValue = currentValue;
        this.description = description;
        this.configType = configType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public ConfigType getConfigType() {
        return configType;
    }

    public void setConfigType(ConfigType configType) {
        this.configType = configType;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getAppliedValue() {
        return appliedValue;
    }

    public void setAppliedValue(String appliedValue) {
        this.appliedValue = appliedValue;
    }

    public Boolean getOverwriteAtStartup() {
        return overwriteAtStartup;
    }

    public void setOverwriteAtStartup(Boolean overwriteAtStartup) {
        this.overwriteAtStartup = overwriteAtStartup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the effective value: appliedValue if set, otherwise currentValue
     */
    public String getEffectiveValue() {
        return appliedValue != null ? appliedValue : currentValue;
    }
}
