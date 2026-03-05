-- Create claimed_cars table for vehicle claim tracking
CREATE TABLE IF NOT EXISTS claimed_cars (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    vehicle_hash TEXT NOT NULL UNIQUE,
    owner_steam_id TEXT NOT NULL,
    owner_name TEXT,
    vehicle_name TEXT,
    script_name TEXT,
    x REAL,
    y REAL,
    last_updated INTEGER,
    migrated BOOLEAN NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create claimed_car_items table for items inside claimed vehicles
CREATE TABLE IF NOT EXISTS claimed_car_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_type TEXT NOT NULL,
    count INTEGER NOT NULL DEFAULT 1,
    container TEXT,
    claimed_car_id INTEGER NOT NULL,
    FOREIGN KEY (claimed_car_id) REFERENCES claimed_cars(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_claimed_cars_vehicle_hash ON claimed_cars(vehicle_hash);
CREATE INDEX IF NOT EXISTS idx_claimed_cars_user_id ON claimed_cars(user_id);
CREATE INDEX IF NOT EXISTS idx_claimed_car_items_claimed_car_id ON claimed_car_items(claimed_car_id);
