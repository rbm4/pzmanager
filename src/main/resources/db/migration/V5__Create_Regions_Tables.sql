-- Create regions table
CREATE TABLE IF NOT EXISTS regions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    code TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    categories TEXT,
    x1 INTEGER NOT NULL,
    x2 INTEGER NOT NULL,
    y1 INTEGER NOT NULL,
    y2 INTEGER NOT NULL,
    z INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    permanent BOOLEAN NOT NULL DEFAULT 0
);

-- Create region_custom_properties table
CREATE TABLE IF NOT EXISTS region_custom_properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    value TEXT NOT NULL,
    region_id INTEGER NOT NULL,
    FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE CASCADE
);
