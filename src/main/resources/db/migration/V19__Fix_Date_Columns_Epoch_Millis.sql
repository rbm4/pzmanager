-- Fix date columns in users, characters, and seasons tables.
-- Hibernate/SQLite stores LocalDateTime as epoch millis in TEXT columns,
-- but the SQLiteDialect expects formatted timestamp strings when reading.
-- This migration converts epoch millis to 'YYYY-MM-DD HH:MM:SS' format
-- and recreates tables with TIMESTAMP column type.

-- ============================================================
-- 1. Fix USERS table (created_at, last_login)
-- ============================================================
CREATE TABLE users_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    steam_id TEXT NOT NULL UNIQUE,
    username TEXT NOT NULL,
    avatar_url TEXT,
    profile_url TEXT,
    created_at TIMESTAMP NOT NULL,
    last_login TIMESTAMP,
    player_stats_id INTEGER,
    role TEXT DEFAULT 'PLAYER',
    pagbank_email TEXT,
    pagbank_cpf TEXT,
    FOREIGN KEY (player_stats_id) REFERENCES player_stats(id)
);

INSERT INTO users_new (id, steam_id, username, avatar_url, profile_url, created_at, last_login, player_stats_id, role, pagbank_email, pagbank_cpf)
SELECT id, steam_id, username, avatar_url, profile_url,
    CASE
        WHEN created_at IS NULL THEN NULL
        WHEN created_at GLOB '[0-9][0-9][0-9][0-9]-*' THEN created_at
        WHEN typeof(created_at) = 'integer' OR (typeof(created_at) = 'text' AND created_at GLOB '[0-9]*' AND length(created_at) >= 10)
            THEN datetime(CAST(created_at AS INTEGER) / 1000, 'unixepoch')
        ELSE created_at
    END,
    CASE
        WHEN last_login IS NULL THEN NULL
        WHEN last_login GLOB '[0-9][0-9][0-9][0-9]-*' THEN last_login
        WHEN typeof(last_login) = 'integer' OR (typeof(last_login) = 'text' AND last_login GLOB '[0-9]*' AND length(last_login) >= 10)
            THEN datetime(CAST(last_login AS INTEGER) / 1000, 'unixepoch')
        ELSE last_login
    END,
    player_stats_id, role, pagbank_email, pagbank_cpf
FROM users;

DROP TABLE users;
ALTER TABLE users_new RENAME TO users;

-- ============================================================
-- 2. Fix CHARACTERS table (created_at, last_update)
-- ============================================================
CREATE TABLE characters_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    season_id INTEGER,
    player_name TEXT NOT NULL,
    server_name TEXT,
    profession TEXT,
    zombie_kills INTEGER DEFAULT 0,
    currency_points INTEGER DEFAULT 0,
    hours_survived REAL DEFAULT 0.0,
    is_dead INTEGER DEFAULT 0,
    last_x INTEGER,
    last_y INTEGER,
    last_z INTEGER,
    last_health INTEGER,
    last_infected INTEGER DEFAULT 0,
    last_in_vehicle INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    last_update TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (season_id) REFERENCES seasons(id)
);

INSERT INTO characters_new (id, user_id, season_id, player_name, server_name, profession,
    zombie_kills, currency_points, hours_survived, is_dead,
    last_x, last_y, last_z, last_health, last_infected, last_in_vehicle,
    created_at, last_update)
SELECT id, user_id, season_id, player_name, server_name, profession,
    zombie_kills, currency_points, hours_survived, is_dead,
    last_x, last_y, last_z, last_health, last_infected, last_in_vehicle,
    CASE
        WHEN created_at IS NULL THEN NULL
        WHEN created_at GLOB '[0-9][0-9][0-9][0-9]-*' THEN created_at
        WHEN typeof(created_at) = 'integer' OR (typeof(created_at) = 'text' AND created_at GLOB '[0-9]*' AND length(created_at) >= 10)
            THEN datetime(CAST(created_at AS INTEGER) / 1000, 'unixepoch')
        ELSE created_at
    END,
    CASE
        WHEN last_update IS NULL THEN NULL
        WHEN last_update GLOB '[0-9][0-9][0-9][0-9]-*' THEN last_update
        WHEN typeof(last_update) = 'integer' OR (typeof(last_update) = 'text' AND last_update GLOB '[0-9]*' AND length(last_update) >= 10)
            THEN datetime(CAST(last_update AS INTEGER) / 1000, 'unixepoch')
        ELSE last_update
    END
FROM characters;

DROP TABLE characters;
ALTER TABLE characters_new RENAME TO characters;

-- ============================================================
-- 3. Fix SEASONS table (start_date, end_date, created_at: TEXT -> TIMESTAMP)
-- ============================================================
CREATE TABLE seasons_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    active INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT (datetime('now'))
);

INSERT INTO seasons_new (id, name, start_date, end_date, active, created_at)
SELECT id, name,
    CASE
        WHEN start_date IS NULL THEN NULL
        WHEN start_date GLOB '[0-9][0-9][0-9][0-9]-*' THEN start_date
        WHEN typeof(start_date) = 'integer' OR (typeof(start_date) = 'text' AND start_date GLOB '[0-9]*' AND length(start_date) >= 10)
            THEN datetime(CAST(start_date AS INTEGER) / 1000, 'unixepoch')
        ELSE start_date
    END,
    CASE
        WHEN end_date IS NULL THEN NULL
        WHEN end_date GLOB '[0-9][0-9][0-9][0-9]-*' THEN end_date
        WHEN typeof(end_date) = 'integer' OR (typeof(end_date) = 'text' AND end_date GLOB '[0-9]*' AND length(end_date) >= 10)
            THEN datetime(CAST(end_date AS INTEGER) / 1000, 'unixepoch')
        ELSE end_date
    END,
    active,
    CASE
        WHEN created_at IS NULL THEN NULL
        WHEN created_at GLOB '[0-9][0-9][0-9][0-9]-*' THEN created_at
        WHEN typeof(created_at) = 'integer' OR (typeof(created_at) = 'text' AND created_at GLOB '[0-9]*' AND length(created_at) >= 10)
            THEN datetime(CAST(created_at AS INTEGER) / 1000, 'unixepoch')
        ELSE created_at
    END
FROM seasons;

DROP TABLE seasons;
ALTER TABLE seasons_new RENAME TO seasons;
