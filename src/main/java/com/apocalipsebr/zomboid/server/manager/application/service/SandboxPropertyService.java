package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEventProperty;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SandboxSetting.ConfigType;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.SandboxSettingRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for managing Zomboid server settings from three configuration sources:
 * <ul>
 *   <li><b>servertest.ini</b> — general server properties (ConfigType.SANDBOX)</li>
 *   <li><b>servertest_SandboxVars.lua</b> — sandbox gameplay variables (ConfigType.SANDBOX_VARS)</li>
 *   <li><b>servertest_spawnregions.lua</b> — spawn region definitions (ConfigType.SPAWN_REGIONS)</li>
 * </ul>
 * On startup, parses all three files, syncs SandboxSetting entities,
 * and overwrites values where appliedValue + overwriteAtStartup are set.
 */
@Service
public class SandboxPropertyService {

    private static final Logger logger = Logger.getLogger(SandboxPropertyService.class.getName());

    @Value("${zomboid.sandbox.file:}")
    private String sandboxFilePath;

    @Value("${zomboid.sandbox.vars.file:}")
    private String sandboxVarsFilePath;

    @Value("${zomboid.sandbox.spawnRegions.file:}")
    private String sandboxSpawnRegionsFilePath;

    @Value("${zomboid.server.path:}")
    private String serverPath;

    private final SandboxSettingRepository sandboxSettingRepository;

    public SandboxPropertyService(SandboxSettingRepository sandboxSettingRepository) {
        this.sandboxSettingRepository = sandboxSettingRepository;
    }

    // ==================== STARTUP LOGIC ====================

    /**
     * On application startup:
     * 1. Parse all three configuration files (ini, SandboxVars.lua, spawnregions.lua)
     * 2. Create/update SandboxSetting entries with currentValue for each
     * 3. For entries with overwriteAtStartup=true and appliedValue!=null,
     *    write the appliedValue back to the respective file
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncSettingsOnStartup() {
        syncIniSettings();
        syncSandboxVarsSettings();
        syncSpawnRegionsSettings();
        logger.info("All sandbox settings sync completed successfully");
    }

    private void syncIniSettings() {
        try {
            Path iniPath = resolveIniPath();
            if (iniPath == null || !Files.exists(iniPath)) {
                logger.warning("Sandbox ini file not found, skipping sync. Path: " + iniPath);
                return;
            }

            List<ParsedEntry> entries = parseIniFile(iniPath);
            logger.info("Parsed " + entries.size() + " properties from servertest.ini");

            syncEntriesToDatabase(entries, ConfigType.SANDBOX);

            List<SandboxSetting> overwrites = sandboxSettingRepository
                    .findByOverwriteAtStartupTrueAndAppliedValueIsNotNullAndConfigType(ConfigType.SANDBOX);

            if (!overwrites.isEmpty()) {
                logger.info("Applying " + overwrites.size() + " overwrite(s) to servertest.ini");
                applyOverwritesToIni(iniPath, overwrites);
                for (SandboxSetting setting : overwrites) {
                    setting.setCurrentValue(setting.getAppliedValue());
                    setting.setUpdatedAt(LocalDateTime.now());
                    sandboxSettingRepository.save(setting);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to sync INI sandbox settings on startup", e);
        }
    }

    private void syncSandboxVarsSettings() {
        try {
            Path luaPath = resolvePath(sandboxVarsFilePath);
            if (luaPath == null || !Files.exists(luaPath)) {
                logger.warning("SandboxVars lua file not found, skipping sync. Path: " + luaPath);
                return;
            }

            List<ParsedEntry> entries = parseSandboxVarsLua(luaPath);
            logger.info("Parsed " + entries.size() + " properties from SandboxVars.lua");

            syncEntriesToDatabase(entries, ConfigType.SANDBOX_VARS);

            List<SandboxSetting> overwrites = sandboxSettingRepository
                    .findByOverwriteAtStartupTrueAndAppliedValueIsNotNullAndConfigType(ConfigType.SANDBOX_VARS);

            if (!overwrites.isEmpty()) {
                logger.info("Applying " + overwrites.size() + " overwrite(s) to SandboxVars.lua");
                applyOverwritesToSandboxVarsLua(luaPath, overwrites);
                for (SandboxSetting setting : overwrites) {
                    setting.setCurrentValue(setting.getAppliedValue());
                    setting.setUpdatedAt(LocalDateTime.now());
                    sandboxSettingRepository.save(setting);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to sync SandboxVars settings on startup", e);
        }
    }

    private void syncSpawnRegionsSettings() {
        try {
            Path luaPath = resolvePath(sandboxSpawnRegionsFilePath);
            if (luaPath == null || !Files.exists(luaPath)) {
                logger.warning("SpawnRegions lua file not found, skipping sync. Path: " + luaPath);
                return;
            }

            List<ParsedEntry> entries = parseSpawnRegionsLua(luaPath);
            logger.info("Parsed " + entries.size() + " spawn regions from spawnregions.lua");

            syncEntriesToDatabase(entries, ConfigType.SPAWN_REGIONS);

            List<SandboxSetting> overwrites = sandboxSettingRepository
                    .findByOverwriteAtStartupTrueAndAppliedValueIsNotNullAndConfigType(ConfigType.SPAWN_REGIONS);

            if (!overwrites.isEmpty()) {
                logger.info("Applying " + overwrites.size() + " overwrite(s) to spawnregions.lua");
                applyOverwritesToSpawnRegionsLua(luaPath, overwrites);
                for (SandboxSetting setting : overwrites) {
                    setting.setCurrentValue(setting.getAppliedValue());
                    setting.setUpdatedAt(LocalDateTime.now());
                    sandboxSettingRepository.save(setting);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to sync SpawnRegions settings on startup", e);
        }
    }

    /**
     * Syncs a list of parsed entries to the database for the given config type.
     */
    private void syncEntriesToDatabase(List<ParsedEntry> entries, ConfigType configType) {
        for (ParsedEntry entry : entries) {
            Optional<SandboxSetting> existing = sandboxSettingRepository
                    .findBySettingKeyAndConfigType(entry.key, configType);
            if (existing.isPresent()) {
                SandboxSetting setting = existing.get();
                setting.setCurrentValue(entry.value);
                setting.setDescription(entry.description);
                setting.setCategory(entry.category);
                setting.setUpdatedAt(LocalDateTime.now());
                sandboxSettingRepository.save(setting);
            } else {
                SandboxSetting setting = new SandboxSetting(entry.key, entry.value, entry.description, configType);
                setting.setCategory(entry.category);
                sandboxSettingRepository.save(setting);
            }
        }
    }

    // ==================== QUERY METHODS ====================

    public Page<SandboxSetting> getSettings(String search, String category, Pageable pageable) {
        return sandboxSettingRepository.search(search, category, (Boolean) null, pageable);
    }

    public Page<SandboxSetting> getSettings(String search, String category, Boolean overwrite, Pageable pageable) {
        return sandboxSettingRepository.search(search, category, overwrite, pageable);
    }

    public Page<SandboxSetting> getSettings(String search, String category, ConfigType configType, Pageable pageable) {
        return sandboxSettingRepository.search(search, category, configType, (Boolean) null, pageable);
    }

    public Page<SandboxSetting> getSettings(String search, String category, ConfigType configType, Boolean overwrite, Pageable pageable) {
        return sandboxSettingRepository.search(search, category, configType, overwrite, pageable);
    }

    public Optional<SandboxSetting> getByKey(String key) {
        return sandboxSettingRepository.findBySettingKey(key);
    }

    public Optional<SandboxSetting> getByKeyAndConfigType(String key, ConfigType configType) {
        return sandboxSettingRepository.findBySettingKeyAndConfigType(key, configType);
    }

    public Optional<SandboxSetting> getById(Long id) {
        return sandboxSettingRepository.findById(id);
    }

    public List<String> getAllCategories() {
        return sandboxSettingRepository.findDistinctCategories();
    }

    public List<String> getCategoriesByConfigType(ConfigType configType) {
        return sandboxSettingRepository.findDistinctCategoriesByConfigType(configType);
    }

    public long getTotalCount() {
        return sandboxSettingRepository.count();
    }

    public long getCountByConfigType(ConfigType configType) {
        return sandboxSettingRepository.countByConfigType(configType);
    }

    public long getModifiedCount() {
        return sandboxSettingRepository.countByAppliedValueIsNotNull();
    }

    public long getOverwriteCount() {
        return sandboxSettingRepository.countByOverwriteAtStartupTrue();
    }

    // ==================== UPDATE METHODS ====================

    /**
     * Update the appliedValue of a setting and optionally mark overwriteAtStartup.
     * Also writes the value immediately to the corresponding config file.
     */
    @Transactional
    public SandboxSetting updateSetting(Long id, String newValue, Boolean overwriteAtStartup) {
        SandboxSetting setting = sandboxSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with id: " + id));

        setting.setAppliedValue(newValue);
        if (overwriteAtStartup != null) {
            setting.setOverwriteAtStartup(overwriteAtStartup);
        }
        setting.setUpdatedAt(LocalDateTime.now());

        // Write change immediately to the corresponding file
        try {
            writeSettingToFile(setting, newValue);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write setting to file: " + setting.getSettingKey(), e);
        }

        return sandboxSettingRepository.save(setting);
    }

    /**
     * Update the appliedValue from the key (for API compatibility).
     * Uses the configType to locate the correct setting when keys may overlap.
     */
    @Transactional
    public SandboxSetting updateSettingByKey(String key, String newValue) {
        SandboxSetting setting = sandboxSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));

        setting.setAppliedValue(newValue);
        setting.setUpdatedAt(LocalDateTime.now());

        try {
            writeSettingToFile(setting, newValue);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write setting to file: " + setting.getSettingKey(), e);
        }

        return sandboxSettingRepository.save(setting);
    }

    @Transactional
    public SandboxSetting updateSettingByKeyAndConfigType(String key, ConfigType configType, String newValue) {
        SandboxSetting setting = sandboxSettingRepository.findBySettingKeyAndConfigType(key, configType)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key + " [" + configType + "]"));

        setting.setAppliedValue(newValue);
        setting.setUpdatedAt(LocalDateTime.now());

        try {
            writeSettingToFile(setting, newValue);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to write setting to file: " + setting.getSettingKey(), e);
        }

        return sandboxSettingRepository.save(setting);
    }

    /**
     * Routes a setting write to the correct config file based on its configType.
     */
    private void writeSettingToFile(SandboxSetting setting, String newValue) throws IOException {
        switch (setting.getConfigType()) {
            case SANDBOX -> {
                Path iniPath = resolveIniPath();
                if (iniPath != null && Files.exists(iniPath)) {
                    applyOverwritesToIni(iniPath, List.of(setting));
                    setting.setCurrentValue(newValue);
                    logger.info("Applied setting " + setting.getSettingKey() + " = " + newValue + " to ini file");
                }
            }
            case SANDBOX_VARS -> {
                Path luaPath = resolvePath(sandboxVarsFilePath);
                if (luaPath != null && Files.exists(luaPath)) {
                    applyOverwritesToSandboxVarsLua(luaPath, List.of(setting));
                    setting.setCurrentValue(newValue);
                    logger.info("Applied setting " + setting.getSettingKey() + " = " + newValue + " to SandboxVars.lua");
                }
            }
            case SPAWN_REGIONS -> {
                Path luaPath = resolvePath(sandboxSpawnRegionsFilePath);
                if (luaPath != null && Files.exists(luaPath)) {
                    applyOverwritesToSpawnRegionsLua(luaPath, List.of(setting));
                    setting.setCurrentValue(newValue);
                    logger.info("Applied setting " + setting.getSettingKey() + " = " + newValue + " to spawnregions.lua");
                }
            }
        }
    }

    /**
     * Toggle the overwriteAtStartup flag for a setting.
     */
    @Transactional
    public SandboxSetting toggleOverwrite(Long id) {
        SandboxSetting setting = sandboxSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with id: " + id));

        setting.setOverwriteAtStartup(!setting.getOverwriteAtStartup());
        setting.setUpdatedAt(LocalDateTime.now());

        return sandboxSettingRepository.save(setting);
    }

    /**
     * Clear the appliedValue (revert to using the ini file's original value).
     */
    @Transactional
    public SandboxSetting clearAppliedValue(Long id) {
        SandboxSetting setting = sandboxSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found with id: " + id));

        setting.setAppliedValue(null);
        setting.setOverwriteAtStartup(false);
        setting.setUpdatedAt(LocalDateTime.now());

        return sandboxSettingRepository.save(setting);
    }

    // ==================== FILE RESOLUTION ====================

    /**
     * Resolves the path to the servertest.ini file.
     */
    private Path resolveIniPath() {
        return resolvePath(sandboxFilePath);
    }

    /**
     * Generic path resolver: supports absolute paths and paths relative to serverPath.
     */
    private Path resolvePath(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            Path absolute = Paths.get(filePath);
            if (absolute.isAbsolute()) {
                return absolute;
            }
            if (serverPath != null && !serverPath.isEmpty()) {
                return Paths.get(serverPath, filePath);
            }
            return absolute;
        }
        return null;
    }

    // ==================== INI FILE PARSING ====================

    /**
     * Parses the servertest.ini file into a list of entries.
     * The INI format:
     *   # Comment (description)
     *   Key=Value
     * Comments preceding a key-value pair are treated as the description.
     */
    private List<ParsedEntry> parseIniFile(Path iniPath) throws IOException {
        List<ParsedEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(iniPath, StandardCharsets.UTF_8);

        StringBuilder descriptionBuffer = new StringBuilder();
        Pattern kvPattern = Pattern.compile("^([A-Za-z0-9_.]+)\\s*=\\s*(.*)$");

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#")) {
                String comment = trimmed.substring(1).trim();
                if (!comment.isEmpty()) {
                    if (descriptionBuffer.length() > 0) {
                        descriptionBuffer.append(" ");
                    }
                    descriptionBuffer.append(comment);
                }
            } else if (!trimmed.isEmpty()) {
                Matcher m = kvPattern.matcher(trimmed);
                if (m.matches()) {
                    String key = m.group(1);
                    String value = m.group(2).trim();
                    String description = descriptionBuffer.toString().trim();
                    String category = categorizeProperty(key);

                    entries.add(new ParsedEntry(key, value, description, category));
                }
                descriptionBuffer.setLength(0);
            } else {
                descriptionBuffer.setLength(0);
            }
        }

        return entries;
    }

    // ==================== SANDBOX VARS LUA PARSING ====================

    /**
     * Parses the servertest_SandboxVars.lua file.
     * Handles top-level and nested section entries (e.g., SprinterScreech.TimeMode).
     * Comments (-- ...) above a field are accumulated as the description.
     */
    private List<ParsedEntry> parseSandboxVarsLua(Path luaPath) throws IOException {
        List<ParsedEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(luaPath, StandardCharsets.UTF_8);

        StringBuilder descriptionBuffer = new StringBuilder();
        String currentSection = null;

        // Patterns for Lua key = value lines
        Pattern kvPattern = Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*=\\s*(.+?)\\s*,?\\s*$");
        // Pattern for opening a nested section: SectionName = {
        Pattern sectionOpenPattern = Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*=\\s*\\{\\s*$");
        // Pattern for closing a section: }
        Pattern sectionClosePattern = Pattern.compile("^\\s*\\}\\s*,?\\s*$");
        // Multiline comment open/close
        Pattern multiCommentOpenPattern = Pattern.compile("^\\s*/\\*");
        Pattern multiCommentClosePattern = Pattern.compile("\\*/\\s*$");

        boolean inMultiLineComment = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // Skip the opening "SandboxVars = {" and closing "}"
            if (trimmed.equals("SandboxVars = {") || (trimmed.equals("}") && currentSection == null)) {
                descriptionBuffer.setLength(0);
                continue;
            }

            // Handle multi-line comments (/* ... */)
            if (inMultiLineComment) {
                if (multiCommentClosePattern.matcher(trimmed).find()) {
                    inMultiLineComment = false;
                }
                continue;
            }
            if (multiCommentOpenPattern.matcher(trimmed).find()) {
                if (!multiCommentClosePattern.matcher(trimmed).find()) {
                    inMultiLineComment = true;
                }
                continue;
            }

            // Comment line
            if (trimmed.startsWith("--")) {
                String comment = trimmed.substring(2).trim();
                if (!comment.isEmpty()) {
                    if (descriptionBuffer.length() > 0) {
                        descriptionBuffer.append(" ");
                    }
                    descriptionBuffer.append(comment);
                }
                continue;
            }

            // Empty line resets description
            if (trimmed.isEmpty()) {
                descriptionBuffer.setLength(0);
                continue;
            }

            // Section close
            Matcher closeMatcher = sectionClosePattern.matcher(trimmed);
            if (closeMatcher.matches() && currentSection != null) {
                currentSection = null;
                descriptionBuffer.setLength(0);
                continue;
            }

            // Section open
            Matcher sectionMatcher = sectionOpenPattern.matcher(trimmed);
            if (sectionMatcher.matches()) {
                currentSection = sectionMatcher.group(1);
                descriptionBuffer.setLength(0);
                continue;
            }

            // Key-value line
            Matcher kvMatcher = kvPattern.matcher(trimmed);
            if (kvMatcher.matches()) {
                String rawKey = kvMatcher.group(1);
                String value = kvMatcher.group(2).trim();

                // Remove trailing comma if present
                if (value.endsWith(",")) {
                    value = value.substring(0, value.length() - 1).trim();
                }

                // Remove surrounding quotes for string values
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                String fullKey = currentSection != null ? currentSection + "." + rawKey : rawKey;
                String description = descriptionBuffer.toString().trim();
                String category = categorizeSandboxVarsProperty(fullKey, currentSection);

                entries.add(new ParsedEntry(fullKey, value, description, category));
                descriptionBuffer.setLength(0);
            }
        }

        return entries;
    }

    /**
     * Categorize a SandboxVars property into a logical group.
     */
    private String categorizeSandboxVarsProperty(String fullKey, String section) {
        if (section != null && !section.isEmpty()) {
            return section;
        }

        String lower = fullKey.toLowerCase();

        if (lower.contains("zombie") || lower.contains("distribution") || lower.contains("migrate")
                || lower.contains("respawn") && !lower.contains("loot")) {
            return "Zumbis";
        }
        if (lower.contains("loot") || lower.contains("rolls") || lower.contains("itemremoval")
                || lower.contains("factor") || lower.contains("removal")) {
            return "Loot";
        }
        if (lower.contains("day") || lower.contains("night") || lower.contains("time")
                || lower.contains("start") && !lower.contains("starter")) {
            return "Tempo";
        }
        if (lower.contains("water") || lower.contains("elec") || lower.contains("alarm")
                && !lower.contains("house")) {
            return "Infraestrutura";
        }
        if (lower.contains("food") || lower.contains("cooking") || lower.contains("nutrition")
                || lower.contains("fridge") || lower.contains("rot")) {
            return "Comida";
        }
        if (lower.contains("farming") || lower.contains("plant") || lower.contains("compost")
                || lower.contains("nature")) {
            return "Agricultura";
        }
        if (lower.contains("erosion")) {
            return "Erosão";
        }
        if (lower.contains("weather") || lower.contains("climate") || lower.contains("fog")
                || lower.contains("temperature") || lower.contains("rain")) {
            return "Clima";
        }
        if (lower.contains("vehicle") || lower.contains("car") || lower.contains("trailer")) {
            return "Veículos";
        }
        if (lower.contains("xp") || lower.contains("skill") || lower.contains("trait")
                || lower.contains("passive") || lower.contains("combat")) {
            return "Habilidades";
        }
        if (lower.contains("fire")) {
            return "Fogo";
        }
        if (lower.contains("safehouse") || lower.contains("safezone")) {
            return "Safehouse";
        }
        if (lower.contains("helicopter") || lower.contains("meta") || lower.contains("event")) {
            return "Eventos";
        }
        if (lower.contains("map") || lower.contains("world")) {
            return "Mapa";
        }

        return "Geral";
    }

    // ==================== SPAWN REGIONS LUA PARSING ====================

    /**
     * Parses the servertest_spawnregions.lua file.
     * Each entry is: { name = "RegionName", file = "media/maps/RegionName/spawnpoints.lua" }
     * The settingKey becomes the name, and currentValue becomes the file path.
     */
    private List<ParsedEntry> parseSpawnRegionsLua(Path luaPath) throws IOException {
        List<ParsedEntry> entries = new ArrayList<>();
        List<String> lines = Files.readAllLines(luaPath, StandardCharsets.UTF_8);

        // Pattern: { name = "...", file = "..." }
        Pattern entryPattern = Pattern.compile(
                "\\{\\s*name\\s*=\\s*\"([^\"]+)\"\\s*,\\s*file\\s*=\\s*\"([^\"]+)\"\\s*\\}");

        StringBuilder descriptionBuffer = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            // Accumulate comments as description
            if (trimmed.startsWith("--")) {
                String comment = trimmed.substring(2).trim();
                if (!comment.isEmpty()) {
                    if (descriptionBuffer.length() > 0) {
                        descriptionBuffer.append(" ");
                    }
                    descriptionBuffer.append(comment);
                }
                continue;
            }

            Matcher m = entryPattern.matcher(trimmed);
            if (m.find()) {
                String name = m.group(1);
                String file = m.group(2);
                String description = descriptionBuffer.toString().trim();

                entries.add(new ParsedEntry(name, file, description, "Spawn Regions"));
                descriptionBuffer.setLength(0);
            } else if (trimmed.isEmpty()) {
                descriptionBuffer.setLength(0);
            }
        }

        return entries;
    }

    // ==================== INI FILE OVERWRITE ====================

    /**
     * Applies overwrite values to the ini file by replacing matching key=value lines.
     */
    private void applyOverwritesToIni(Path iniPath, List<SandboxSetting> overwrites) throws IOException {
        Map<String, String> overwriteMap = new HashMap<>();
        for (SandboxSetting setting : overwrites) {
            String value = setting.getAppliedValue();
            if (value != null) {
                overwriteMap.put(setting.getSettingKey(), value);
            }
        }

        if (overwriteMap.isEmpty()) return;

        Path backupPath = iniPath.resolveSibling(iniPath.getFileName() + ".backup");
        Files.copy(iniPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        List<String> lines = Files.readAllLines(iniPath, StandardCharsets.UTF_8);
        List<String> outputLines = new ArrayList<>();
        Pattern kvPattern = Pattern.compile("^([A-Za-z0-9_.]+)\\s*=\\s*(.*)$");

        for (String line : lines) {
            Matcher m = kvPattern.matcher(line.trim());
            if (m.matches()) {
                String key = m.group(1);
                if (overwriteMap.containsKey(key)) {
                    outputLines.add(key + "=" + overwriteMap.get(key));
                    logger.info("Overwrote ini property: " + key + " = " + overwriteMap.get(key));
                    continue;
                }
            }
            outputLines.add(line);
        }

        Files.write(iniPath, outputLines, StandardCharsets.UTF_8);
        logger.info("Successfully wrote " + overwriteMap.size() + " overwrite(s) to " + iniPath);
    }

    // ==================== SANDBOX VARS LUA OVERWRITE ====================

    /**
     * Applies overwrite values to the SandboxVars.lua file.
     * Handles both top-level keys (e.g. Zombies) and nested keys (e.g. SprinterScreech.TimeMode).
     */
    private void applyOverwritesToSandboxVarsLua(Path luaPath, List<SandboxSetting> overwrites) throws IOException {
        Map<String, String> overwriteMap = new HashMap<>();
        for (SandboxSetting setting : overwrites) {
            String value = setting.getAppliedValue();
            if (value != null) {
                overwriteMap.put(setting.getSettingKey(), value);
            }
        }

        if (overwriteMap.isEmpty()) return;

        Path backupPath = luaPath.resolveSibling(luaPath.getFileName() + ".backup");
        Files.copy(luaPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        List<String> lines = Files.readAllLines(luaPath, StandardCharsets.UTF_8);
        List<String> outputLines = new ArrayList<>();

        String currentSection = null;
        Pattern kvPattern = Pattern.compile("^(\\s*)([A-Za-z0-9_]+)(\\s*=\\s*)(.+?)(\\s*,?\\s*)$");
        Pattern sectionOpenPattern = Pattern.compile("^\\s*([A-Za-z0-9_]+)\\s*=\\s*\\{\\s*$");
        Pattern sectionClosePattern = Pattern.compile("^\\s*\\}\\s*,?\\s*$");

        for (String line : lines) {
            String trimmed = line.trim();

            // Track section open/close
            Matcher sectionMatcher = sectionOpenPattern.matcher(trimmed);
            if (sectionMatcher.matches()) {
                currentSection = sectionMatcher.group(1);
                outputLines.add(line);
                continue;
            }

            Matcher closeMatcher = sectionClosePattern.matcher(trimmed);
            if (closeMatcher.matches() && currentSection != null) {
                currentSection = null;
                outputLines.add(line);
                continue;
            }

            // Try to match key=value
            Matcher kvMatcher = kvPattern.matcher(line);
            if (kvMatcher.matches()) {
                String indent = kvMatcher.group(1);
                String rawKey = kvMatcher.group(2);
                String separator = kvMatcher.group(3);
                String oldValue = kvMatcher.group(4);
                String trailing = kvMatcher.group(5);

                if (overwriteMap.containsKey(rawKey)) {
                    String newValue = overwriteMap.get(rawKey);
                    String formattedValue = formatLuaValue(newValue, oldValue);
                    outputLines.add(indent + rawKey + separator + formattedValue + trailing);
                    logger.info("Overwrote SandboxVars property: " + rawKey + " = " + newValue);
                    continue;
                }
            }

            outputLines.add(line);
        }

        Files.write(luaPath, outputLines, StandardCharsets.UTF_8);
        logger.info("Successfully wrote " + overwriteMap.size() + " overwrite(s) to " + luaPath);
    }

    /**
     * Formats a value for Lua output, preserving the original value type (string, boolean, number).
     */
    private String formatLuaValue(String newValue, String oldValue) {
        String oldTrimmed = oldValue.trim();
        if (oldTrimmed.endsWith(",")) {
            oldTrimmed = oldTrimmed.substring(0, oldTrimmed.length() - 1).trim();
        }

        // If old value was a quoted string, quote the new one
        if (oldTrimmed.startsWith("\"") && oldTrimmed.endsWith("\"")) {
            return "\"" + newValue + "\"";
        }

        // Boolean values
        if ("true".equalsIgnoreCase(newValue) || "false".equalsIgnoreCase(newValue)) {
            return newValue.toLowerCase();
        }

        // Number values: return as-is
        return newValue;
    }

    // ==================== SPAWN REGIONS LUA OVERWRITE ====================

    /**
     * Applies overwrite values to the spawnregions.lua file.
     * The settingKey is the region name, and the appliedValue is the new file path.
     */
    private void applyOverwritesToSpawnRegionsLua(Path luaPath, List<SandboxSetting> overwrites) throws IOException {
        Map<String, String> overwriteMap = new HashMap<>();
        for (SandboxSetting setting : overwrites) {
            String value = setting.getAppliedValue();
            if (value != null) {
                overwriteMap.put(setting.getSettingKey(), value);
            }
        }

        if (overwriteMap.isEmpty()) return;

        Path backupPath = luaPath.resolveSibling(luaPath.getFileName() + ".backup");
        Files.copy(luaPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        List<String> lines = Files.readAllLines(luaPath, StandardCharsets.UTF_8);
        List<String> outputLines = new ArrayList<>();

        Pattern entryPattern = Pattern.compile(
                "(\\{\\s*name\\s*=\\s*\"([^\"]+)\"\\s*,\\s*file\\s*=\\s*\")([^\"]+)(\"\\s*\\})");

        for (String line : lines) {
            Matcher m = entryPattern.matcher(line);
            if (m.find()) {
                String regionName = m.group(2);
                if (overwriteMap.containsKey(regionName)) {
                    String newFile = overwriteMap.get(regionName);
                    String newLine = line.substring(0, m.start()) + m.group(1) + newFile + m.group(4) + line.substring(m.end());
                    outputLines.add(newLine);
                    logger.info("Overwrote SpawnRegion: " + regionName + " -> " + newFile);
                    continue;
                }
            }
            outputLines.add(line);
        }

        Files.write(luaPath, outputLines, StandardCharsets.UTF_8);
        logger.info("Successfully wrote " + overwriteMap.size() + " overwrite(s) to " + luaPath);
    }

    // ==================== INI CATEGORIZATION ====================
    private String categorizeProperty(String key) {
        String lower = key.toLowerCase();

        if (lower.startsWith("pvp") || lower.contains("meledamage") || lower.contains("firearmdamage")
                || lower.equals("war") || lower.startsWith("war")) {
            return "PVP & Combate";
        }
        if (lower.startsWith("safehouse") || lower.startsWith("safezone") || lower.startsWith("safehouseallow")
                || lower.equals("adminsafehouse") || lower.equals("playersafehouse")
                || lower.startsWith("maxsafezone")) {
            return "Safehouse";
        }
        if (lower.startsWith("safety")) {
            return "Sistema de Segurança";
        }
        if (lower.startsWith("rcon")) {
            return "RCON";
        }
        if (lower.startsWith("discord") || lower.contains("webhook")) {
            return "Discord";
        }
        if (lower.startsWith("voice") || lower.contains("voip")) {
            return "VOIP";
        }
        if (lower.startsWith("anticheat")) {
            return "Anti-Cheat";
        }
        if (lower.startsWith("backup")) {
            return "Backup";
        }
        if (lower.startsWith("faction")) {
            return "Facções";
        }
        if (lower.startsWith("disableradio")) {
            return "Rádio";
        }
        if (lower.contains("vehicle") || lower.contains("trailer") || lower.contains("towing")
                || lower.equals("carengineattraction") || lower.startsWith("carengineattractionmodifier")) {
            return "Veículos";
        }
        if (lower.startsWith("steam") || lower.equals("upnp")) {
            return "Steam & Rede";
        }
        if (lower.startsWith("login") || lower.startsWith("denylogin")) {
            return "Login";
        }
        if (lower.startsWith("spawn") || lower.startsWith("playerrespawn")) {
            return "Spawn & Respawn";
        }
        if (lower.contains("chat") || lower.contains("message") && !lower.contains("welcome")) {
            return "Chat";
        }
        if (lower.equals("map") || lower.equals("seed") || lower.startsWith("mapremote")) {
            return "Mapa";
        }
        if (lower.startsWith("sleep") || lower.startsWith("fastforward") || lower.startsWith("knockeddown")) {
            return "Jogabilidade";
        }
        if (lower.startsWith("mod") || lower.startsWith("workshop")) {
            return "Mods & Workshop";
        }
        if (lower.startsWith("max") || lower.equals("defaultport") || lower.equals("udpport")
                || lower.startsWith("public") || lower.equals("open") || lower.equals("password")
                || lower.equals("resetid") || lower.equals("serverplayerid") || lower.startsWith("server_browser")) {
            return "Servidor";
        }
        if (lower.startsWith("badword") || lower.startsWith("goodword")) {
            return "Filtro de Palavras";
        }
        if (lower.contains("player") || lower.contains("username") || lower.contains("display")
                || lower.contains("scoreboard") || lower.contains("disguise")) {
            return "Jogador";
        }
        if (lower.contains("zombie") || lower.equals("switchzombiesownershipeachupdate")) {
            return "Zumbis";
        }

        return "Geral";
    }

    // ==================== INTERNAL ====================

    /**
     * Simple record to hold parsed entries from any config file.
     */
    private record ParsedEntry(String key, String value, String description, String category) {}
}
