-- Add missing columns to cars table
ALTER TABLE cars ADD COLUMN vehicle_script TEXT NOT NULL DEFAULT 'Base.CarNormal';
ALTER TABLE cars ADD COLUMN trunk_size INTEGER;
ALTER TABLE cars ADD COLUMN seats INTEGER;
ALTER TABLE cars ADD COLUMN doors INTEGER;
