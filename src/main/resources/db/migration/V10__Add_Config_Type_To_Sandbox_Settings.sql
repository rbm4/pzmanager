-- Recreate table to change unique constraint from (setting_key) to (setting_key, config_type)

-- 1. Create new table with the desired schema
CREATE TABLE sandbox_settings_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    setting_key TEXT NOT NULL,
    config_type TEXT NOT NULL DEFAULT 'SANDBOX',
    current_value TEXT,
    applied_value TEXT,
    overwrite_at_startup BOOLEAN NOT NULL DEFAULT 0,
    description TEXT,
    category TEXT,
    updated_at TEXT,
    created_at TEXT NOT NULL,
    UNIQUE(setting_key, config_type)
);

-- 2. Copy existing data (all existing rows get config_type = 'SANDBOX')
INSERT INTO sandbox_settings_new (id, setting_key, config_type, current_value, applied_value, overwrite_at_startup, description, category, updated_at, created_at)
SELECT id, setting_key, 'SANDBOX', current_value, applied_value, overwrite_at_startup, description, category, updated_at, created_at
FROM sandbox_settings;

-- 3. Drop old table
DROP TABLE sandbox_settings;

-- 4. Rename new table
ALTER TABLE sandbox_settings_new RENAME TO sandbox_settings;
