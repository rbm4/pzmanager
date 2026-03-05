package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "characters")
public class Character {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;
    
    @Column(name = "player_name", nullable = false)
    private String playerName;
    
    @Column(name = "server_name")
    private String serverName;
    
    @Column(name = "profession")
    private String profession;
    
    @Column(name = "zombie_kills")
    private Integer zombieKills = 0;
    
    @Column(name = "currency_points")
    private Integer currencyPoints = 0;
    
    @Column(name = "hours_survived")
    private Double hoursSurvived = 0.0;
    
    @Column(name = "is_dead")
    private Boolean isDead = false;
    
    @Column(name = "last_x")
    private Integer lastX;
    
    @Column(name = "last_y")
    private Integer lastY;
    
    @Column(name = "last_z")
    private Integer lastZ;
    
    @Column(name = "last_health")
    private Integer lastHealth;
    
    @Column(name = "last_infected")
    private Boolean lastInfected = false;
    
    @Column(name = "last_in_vehicle")
    private Boolean lastInVehicle = false;

    // ── Skill experience fields (stores highest observed XP) ──
    @Column(name = "skill_cooking")
    private Double skillCooking = 0.0;

    @Column(name = "skill_fitness")
    private Double skillFitness = 0.0;

    @Column(name = "skill_strength")
    private Double skillStrength = 0.0;

    @Column(name = "skill_blunt")
    private Double skillBlunt = 0.0;

    @Column(name = "skill_axe")
    private Double skillAxe = 0.0;

    @Column(name = "skill_lightfoot")
    private Double skillLightfoot = 0.0;

    @Column(name = "skill_nimble")
    private Double skillNimble = 0.0;

    @Column(name = "skill_sprinting")
    private Double skillSprinting = 0.0;

    @Column(name = "skill_sneak")
    private Double skillSneak = 0.0;

    @Column(name = "skill_woodwork")
    private Double skillWoodwork = 0.0;

    @Column(name = "skill_aiming")
    private Double skillAiming = 0.0;

    @Column(name = "skill_reloading")
    private Double skillReloading = 0.0;

    @Column(name = "skill_farming")
    private Double skillFarming = 0.0;

    @Column(name = "skill_fishing")
    private Double skillFishing = 0.0;

    @Column(name = "skill_trapping")
    private Double skillTrapping = 0.0;

    @Column(name = "skill_plant_scavenging")
    private Double skillPlantScavenging = 0.0;

    @Column(name = "skill_doctor")
    private Double skillDoctor = 0.0;

    @Column(name = "skill_electricity")
    private Double skillElectricity = 0.0;

    @Column(name = "skill_blacksmith")
    private Double skillBlacksmith = 0.0;

    @Column(name = "skill_metal_welding")
    private Double skillMetalWelding = 0.0;

    @Column(name = "skill_mechanics")
    private Double skillMechanics = 0.0;

    @Column(name = "skill_spear")
    private Double skillSpear = 0.0;

    @Column(name = "skill_maintenance")
    private Double skillMaintenance = 0.0;

    @Column(name = "skill_small_blade")
    private Double skillSmallBlade = 0.0;

    @Column(name = "skill_long_blade")
    private Double skillLongBlade = 0.0;

    @Column(name = "skill_small_blunt")
    private Double skillSmallBlunt = 0.0;

    @Column(name = "skill_tailoring")
    private Double skillTailoring = 0.0;

    @Column(name = "skill_tracking")
    private Double skillTracking = 0.0;

    @Column(name = "skill_husbandry")
    private Double skillHusbandry = 0.0;

    @Column(name = "skill_flint_knapping")
    private Double skillFlintKnapping = 0.0;

    @Column(name = "skill_masonry")
    private Double skillMasonry = 0.0;

    @Column(name = "skill_pottery")
    private Double skillPottery = 0.0;

    @Column(name = "skill_carving")
    private Double skillCarving = 0.0;

    @Column(name = "skill_butchering")
    private Double skillButchering = 0.0;

    @Column(name = "skill_glassmaking")
    private Double skillGlassmaking = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;
    
    @Column(name = "last_update", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastUpdate;

    public Character() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdate = LocalDateTime.now();
    }

    public Character(User user, String playerName, String serverName) {
        this();
        this.user = user;
        this.playerName = playerName;
        this.serverName = serverName;
    }

    public Character(User user, String playerName, String serverName, Season season) {
        this(user, playerName, serverName);
        this.season = season;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public Integer getZombieKills() {
        return zombieKills;
    }

    public void setZombieKills(Integer zombieKills) {
        this.zombieKills = zombieKills;
    }

    public Integer getCurrencyPoints() {
        return currencyPoints;
    }

    public void setCurrencyPoints(Integer currencyPoints) {
        this.currencyPoints = currencyPoints;
    }

    public Double getHoursSurvived() {
        return hoursSurvived;
    }

    public void setHoursSurvived(Double hoursSurvived) {
        this.hoursSurvived = hoursSurvived;
    }

    public Boolean getIsDead() {
        return isDead;
    }

    public void setIsDead(Boolean isDead) {
        this.isDead = isDead;
    }

    public Integer getLastX() {
        return lastX;
    }

    public void setLastX(Integer lastX) {
        this.lastX = lastX;
    }

    public Integer getLastY() {
        return lastY;
    }

    public void setLastY(Integer lastY) {
        this.lastY = lastY;
    }

    public Integer getLastZ() {
        return lastZ;
    }

    public void setLastZ(Integer lastZ) {
        this.lastZ = lastZ;
    }

    public Integer getLastHealth() {
        return lastHealth;
    }

    public void setLastHealth(Integer lastHealth) {
        this.lastHealth = lastHealth;
    }

    public Boolean getLastInfected() {
        return lastInfected;
    }

    public void setLastInfected(Boolean lastInfected) {
        this.lastInfected = lastInfected;
    }

    public Boolean getLastInVehicle() {
        return lastInVehicle;
    }

    public void setLastInVehicle(Boolean lastInVehicle) {
        this.lastInVehicle = lastInVehicle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    // ── Skill getters and setters ──

    public Double getSkillCooking() { return skillCooking; }
    public void setSkillCooking(Double skillCooking) { this.skillCooking = skillCooking; }

    public Double getSkillFitness() { return skillFitness; }
    public void setSkillFitness(Double skillFitness) { this.skillFitness = skillFitness; }

    public Double getSkillStrength() { return skillStrength; }
    public void setSkillStrength(Double skillStrength) { this.skillStrength = skillStrength; }

    public Double getSkillBlunt() { return skillBlunt; }
    public void setSkillBlunt(Double skillBlunt) { this.skillBlunt = skillBlunt; }

    public Double getSkillAxe() { return skillAxe; }
    public void setSkillAxe(Double skillAxe) { this.skillAxe = skillAxe; }

    public Double getSkillLightfoot() { return skillLightfoot; }
    public void setSkillLightfoot(Double skillLightfoot) { this.skillLightfoot = skillLightfoot; }

    public Double getSkillNimble() { return skillNimble; }
    public void setSkillNimble(Double skillNimble) { this.skillNimble = skillNimble; }

    public Double getSkillSprinting() { return skillSprinting; }
    public void setSkillSprinting(Double skillSprinting) { this.skillSprinting = skillSprinting; }

    public Double getSkillSneak() { return skillSneak; }
    public void setSkillSneak(Double skillSneak) { this.skillSneak = skillSneak; }

    public Double getSkillWoodwork() { return skillWoodwork; }
    public void setSkillWoodwork(Double skillWoodwork) { this.skillWoodwork = skillWoodwork; }

    public Double getSkillAiming() { return skillAiming; }
    public void setSkillAiming(Double skillAiming) { this.skillAiming = skillAiming; }

    public Double getSkillReloading() { return skillReloading; }
    public void setSkillReloading(Double skillReloading) { this.skillReloading = skillReloading; }

    public Double getSkillFarming() { return skillFarming; }
    public void setSkillFarming(Double skillFarming) { this.skillFarming = skillFarming; }

    public Double getSkillFishing() { return skillFishing; }
    public void setSkillFishing(Double skillFishing) { this.skillFishing = skillFishing; }

    public Double getSkillTrapping() { return skillTrapping; }
    public void setSkillTrapping(Double skillTrapping) { this.skillTrapping = skillTrapping; }

    public Double getSkillPlantScavenging() { return skillPlantScavenging; }
    public void setSkillPlantScavenging(Double skillPlantScavenging) { this.skillPlantScavenging = skillPlantScavenging; }

    public Double getSkillDoctor() { return skillDoctor; }
    public void setSkillDoctor(Double skillDoctor) { this.skillDoctor = skillDoctor; }

    public Double getSkillElectricity() { return skillElectricity; }
    public void setSkillElectricity(Double skillElectricity) { this.skillElectricity = skillElectricity; }

    public Double getSkillBlacksmith() { return skillBlacksmith; }
    public void setSkillBlacksmith(Double skillBlacksmith) { this.skillBlacksmith = skillBlacksmith; }

    public Double getSkillMetalWelding() { return skillMetalWelding; }
    public void setSkillMetalWelding(Double skillMetalWelding) { this.skillMetalWelding = skillMetalWelding; }

    public Double getSkillMechanics() { return skillMechanics; }
    public void setSkillMechanics(Double skillMechanics) { this.skillMechanics = skillMechanics; }

    public Double getSkillSpear() { return skillSpear; }
    public void setSkillSpear(Double skillSpear) { this.skillSpear = skillSpear; }

    public Double getSkillMaintenance() { return skillMaintenance; }
    public void setSkillMaintenance(Double skillMaintenance) { this.skillMaintenance = skillMaintenance; }

    public Double getSkillSmallBlade() { return skillSmallBlade; }
    public void setSkillSmallBlade(Double skillSmallBlade) { this.skillSmallBlade = skillSmallBlade; }

    public Double getSkillLongBlade() { return skillLongBlade; }
    public void setSkillLongBlade(Double skillLongBlade) { this.skillLongBlade = skillLongBlade; }

    public Double getSkillSmallBlunt() { return skillSmallBlunt; }
    public void setSkillSmallBlunt(Double skillSmallBlunt) { this.skillSmallBlunt = skillSmallBlunt; }

    public Double getSkillTailoring() { return skillTailoring; }
    public void setSkillTailoring(Double skillTailoring) { this.skillTailoring = skillTailoring; }

    public Double getSkillTracking() { return skillTracking; }
    public void setSkillTracking(Double skillTracking) { this.skillTracking = skillTracking; }

    public Double getSkillHusbandry() { return skillHusbandry; }
    public void setSkillHusbandry(Double skillHusbandry) { this.skillHusbandry = skillHusbandry; }

    public Double getSkillFlintKnapping() { return skillFlintKnapping; }
    public void setSkillFlintKnapping(Double skillFlintKnapping) { this.skillFlintKnapping = skillFlintKnapping; }

    public Double getSkillMasonry() { return skillMasonry; }
    public void setSkillMasonry(Double skillMasonry) { this.skillMasonry = skillMasonry; }

    public Double getSkillPottery() { return skillPottery; }
    public void setSkillPottery(Double skillPottery) { this.skillPottery = skillPottery; }

    public Double getSkillCarving() { return skillCarving; }
    public void setSkillCarving(Double skillCarving) { this.skillCarving = skillCarving; }

    public Double getSkillButchering() { return skillButchering; }
    public void setSkillButchering(Double skillButchering) { this.skillButchering = skillButchering; }

    public Double getSkillGlassmaking() { return skillGlassmaking; }
    public void setSkillGlassmaking(Double skillGlassmaking) { this.skillGlassmaking = skillGlassmaking; }

    /**
     * Updates all skill fields keeping the maximum between the current stored value and the incoming values.
     * This ensures we always persist the highest XP observed for each skill.
     */
    public void updateSkillsKeepMax(Map<String, Double> incomingSkills) {
        if (incomingSkills == null || incomingSkills.isEmpty()) return;

        this.skillCooking = maxSkill(this.skillCooking, incomingSkills.get("Cooking"));
        this.skillFitness = maxSkill(this.skillFitness, incomingSkills.get("Fitness"));
        this.skillStrength = maxSkill(this.skillStrength, incomingSkills.get("Strength"));
        this.skillBlunt = maxSkill(this.skillBlunt, incomingSkills.get("Blunt"));
        this.skillAxe = maxSkill(this.skillAxe, incomingSkills.get("Axe"));
        this.skillLightfoot = maxSkill(this.skillLightfoot, incomingSkills.get("Lightfoot"));
        this.skillNimble = maxSkill(this.skillNimble, incomingSkills.get("Nimble"));
        this.skillSprinting = maxSkill(this.skillSprinting, incomingSkills.get("Sprinting"));
        this.skillSneak = maxSkill(this.skillSneak, incomingSkills.get("Sneak"));
        this.skillWoodwork = maxSkill(this.skillWoodwork, incomingSkills.get("Woodwork"));
        this.skillAiming = maxSkill(this.skillAiming, incomingSkills.get("Aiming"));
        this.skillReloading = maxSkill(this.skillReloading, incomingSkills.get("Reloading"));
        this.skillFarming = maxSkill(this.skillFarming, incomingSkills.get("Farming"));
        this.skillFishing = maxSkill(this.skillFishing, incomingSkills.get("Fishing"));
        this.skillTrapping = maxSkill(this.skillTrapping, incomingSkills.get("Trapping"));
        this.skillPlantScavenging = maxSkill(this.skillPlantScavenging, incomingSkills.get("PlantScavenging"));
        this.skillDoctor = maxSkill(this.skillDoctor, incomingSkills.get("Doctor"));
        this.skillElectricity = maxSkill(this.skillElectricity, incomingSkills.get("Electricity"));
        this.skillBlacksmith = maxSkill(this.skillBlacksmith, incomingSkills.get("Blacksmith"));
        this.skillMetalWelding = maxSkill(this.skillMetalWelding, incomingSkills.get("MetalWelding"));
        this.skillMechanics = maxSkill(this.skillMechanics, incomingSkills.get("Mechanics"));
        this.skillSpear = maxSkill(this.skillSpear, incomingSkills.get("Spear"));
        this.skillMaintenance = maxSkill(this.skillMaintenance, incomingSkills.get("Maintenance"));
        this.skillSmallBlade = maxSkill(this.skillSmallBlade, incomingSkills.get("SmallBlade"));
        this.skillLongBlade = maxSkill(this.skillLongBlade, incomingSkills.get("LongBlade"));
        this.skillSmallBlunt = maxSkill(this.skillSmallBlunt, incomingSkills.get("SmallBlunt"));
        this.skillTailoring = maxSkill(this.skillTailoring, incomingSkills.get("Tailoring"));
        this.skillTracking = maxSkill(this.skillTracking, incomingSkills.get("Tracking"));
        this.skillHusbandry = maxSkill(this.skillHusbandry, incomingSkills.get("Husbandry"));
        this.skillFlintKnapping = maxSkill(this.skillFlintKnapping, incomingSkills.get("FlintKnapping"));
        this.skillMasonry = maxSkill(this.skillMasonry, incomingSkills.get("Masonry"));
        this.skillPottery = maxSkill(this.skillPottery, incomingSkills.get("Pottery"));
        this.skillCarving = maxSkill(this.skillCarving, incomingSkills.get("Carving"));
        this.skillButchering = maxSkill(this.skillButchering, incomingSkills.get("Butchering"));
        this.skillGlassmaking = maxSkill(this.skillGlassmaking, incomingSkills.get("Glassmaking"));
    }

    private static double maxSkill(Double current, Double incoming) {
        double c = (current != null) ? current : 0.0;
        double i = (incoming != null) ? incoming : 0.0;
        return Math.max(c, i);
    }
}
