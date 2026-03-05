-- Add skill experience columns to characters table
-- Each skill stores the highest XP value observed (REAL to handle decimals)

ALTER TABLE characters ADD COLUMN skill_cooking REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_fitness REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_strength REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_blunt REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_axe REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_lightfoot REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_nimble REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_sprinting REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_sneak REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_woodwork REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_aiming REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_reloading REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_farming REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_fishing REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_trapping REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_plant_scavenging REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_doctor REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_electricity REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_blacksmith REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_metal_welding REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_mechanics REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_spear REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_maintenance REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_small_blade REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_long_blade REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_small_blunt REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_tailoring REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_tracking REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_husbandry REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_flint_knapping REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_masonry REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_pottery REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_carving REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_butchering REAL DEFAULT 0;
ALTER TABLE characters ADD COLUMN skill_glassmaking REAL DEFAULT 0;
