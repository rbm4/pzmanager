-- Create sandbox_settings table
CREATE TABLE IF NOT EXISTS sandbox_settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    setting_key TEXT NOT NULL UNIQUE,
    current_value TEXT,
    applied_value TEXT,
    overwrite_at_startup BOOLEAN NOT NULL DEFAULT 0,
    description TEXT,
    category TEXT,
    updated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
