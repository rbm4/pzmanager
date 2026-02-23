-- Create seasons table
CREATE TABLE IF NOT EXISTS seasons (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT,
    active INTEGER NOT NULL DEFAULT 0,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

-- Insert first season (active)
INSERT INTO seasons (name, start_date, active, created_at)
VALUES ('Temporada 1', datetime('now'), 1, datetime('now'));

-- Add season_id column to characters
ALTER TABLE characters ADD COLUMN season_id INTEGER REFERENCES seasons(id);

-- Assign all existing characters to season 1
UPDATE characters SET season_id = 1;
