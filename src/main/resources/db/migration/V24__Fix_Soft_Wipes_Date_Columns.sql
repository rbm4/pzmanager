-- Fix soft_wipes date columns: TEXT -> TIMESTAMP
-- Hibernate/SQLite stores LocalDateTime as epoch millis in TEXT columns,
-- but the SQLiteDialect expects formatted timestamp strings when reading.
-- This converts epoch millis to 'YYYY-MM-DD HH:MM:SS' format and uses TIMESTAMP type.

CREATE TABLE soft_wipes_new (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id        INTEGER NOT NULL REFERENCES users(id),
    status         TEXT    NOT NULL DEFAULT 'WAITING_RESTART',
    x1             INTEGER NOT NULL,
    y1             INTEGER NOT NULL,
    x2             INTEGER NOT NULL,
    y2             INTEGER NOT NULL,
    cost           INTEGER NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    executed_at    TIMESTAMP,
    bins_deleted   INTEGER DEFAULT 0,
    bins_protected INTEGER DEFAULT 0,
    error_message  TEXT
);

INSERT INTO soft_wipes_new (id, user_id, status, x1, y1, x2, y2, cost, created_at, executed_at, bins_deleted, bins_protected, error_message)
SELECT id, user_id, status, x1, y1, x2, y2, cost,
    CASE
        WHEN created_at IS NULL THEN NULL
        WHEN created_at GLOB '[0-9][0-9][0-9][0-9]-*' THEN created_at
        WHEN typeof(created_at) = 'integer' OR (typeof(created_at) = 'text' AND created_at GLOB '[0-9]*' AND length(created_at) >= 10)
            THEN datetime(CAST(created_at AS INTEGER) / 1000, 'unixepoch')
        ELSE created_at
    END,
    CASE
        WHEN executed_at IS NULL THEN NULL
        WHEN executed_at GLOB '[0-9][0-9][0-9][0-9]-*' THEN executed_at
        WHEN typeof(executed_at) = 'integer' OR (typeof(executed_at) = 'text' AND executed_at GLOB '[0-9]*' AND length(executed_at) >= 10)
            THEN datetime(CAST(executed_at AS INTEGER) / 1000, 'unixepoch')
        ELSE executed_at
    END,
    bins_deleted, bins_protected, error_message
FROM soft_wipes;

DROP TABLE soft_wipes;
ALTER TABLE soft_wipes_new RENAME TO soft_wipes;

CREATE INDEX idx_soft_wipes_status ON soft_wipes(status);
CREATE INDEX idx_soft_wipes_user_id ON soft_wipes(user_id);
