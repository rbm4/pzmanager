-- V22: Replace 'migrated' column with 'preserved_for_migration' in claimed_cars
-- SQLite table rebuild pattern (DROP COLUMN not supported in older SQLite)

-- 1. Rename existing table
ALTER TABLE claimed_cars RENAME TO claimed_cars_old;

-- 2. Create new table without 'migrated', with 'preserved_for_migration'
CREATE TABLE claimed_cars (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_hash TEXT NOT NULL UNIQUE,
    owner_steam_id TEXT NOT NULL,
    owner_name TEXT,
    vehicle_name TEXT,
    script_name TEXT,
    x REAL,
    y REAL,
    last_updated INTEGER,
    preserved_for_migration BOOLEAN NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 3. Copy data from old table (migrated -> preserved_for_migration)
INSERT INTO claimed_cars (id, vehicle_hash, owner_steam_id, owner_name, vehicle_name, script_name,
                          x, y, last_updated, preserved_for_migration, created_at, updated_at, user_id)
SELECT id, vehicle_hash, owner_steam_id, owner_name, vehicle_name, script_name,
       x, y, last_updated, migrated, created_at, updated_at, user_id
FROM claimed_cars_old;

-- 4. Drop old table
DROP TABLE claimed_cars_old;

-- 5. Recreate indexes
CREATE INDEX IF NOT EXISTS idx_claimed_cars_vehicle_hash ON claimed_cars(vehicle_hash);
CREATE INDEX IF NOT EXISTS idx_claimed_cars_user_id ON claimed_cars(user_id);
CREATE INDEX IF NOT EXISTS idx_claimed_cars_preserved ON claimed_cars(preserved_for_migration);
