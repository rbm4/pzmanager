-- Redo sandbox_settings table with config_type column and correct column types
-- This fixes V10 which used TEXT instead of TIMESTAMP for date columns

-- 1. Drop the broken table (from V10) or original (from V8) — either way, start fresh
DROP TABLE IF EXISTS sandbox_settings_new;
DROP TABLE IF EXISTS sandbox_settings;

-- 2. Create the correct table from scratch
CREATE TABLE sandbox_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    setting_key TEXT NOT NULL,
    config_type TEXT NOT NULL DEFAULT 'SANDBOX',
    current_value TEXT,
    applied_value TEXT,
    overwrite_at_startup BOOLEAN NOT NULL DEFAULT 0,
    description TEXT,
    category TEXT,
    updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(setting_key, config_type)
);
