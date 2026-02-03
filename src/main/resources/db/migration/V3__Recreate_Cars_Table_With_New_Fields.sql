-- Recreate cars table with all columns including new ones
PRAGMA foreign_keys=OFF;

CREATE TABLE cars_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    model TEXT NOT NULL,
    vehicle_script TEXT NOT NULL DEFAULT 'Base.CarNormal',
    value INTEGER NOT NULL,
    trunk_size INTEGER,
    seats INTEGER,
    doors INTEGER,
    description TEXT,
    images TEXT,
    available BOOLEAN NOT NULL DEFAULT 1
);

INSERT INTO cars_new (id, name, model, value, description, images, available)
SELECT id, name, model, value, description, images, COALESCE(available, 1) FROM cars;

DROP TABLE cars;

ALTER TABLE cars_new RENAME TO cars;

PRAGMA foreign_keys=ON;
