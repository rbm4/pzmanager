-- V25: Create character_migrations table for XP migration tracking
CREATE TABLE IF NOT EXISTS character_migrations (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    character_id    INTEGER NOT NULL,
    user_id         INTEGER NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'PENDING',
    snapshot_skills TEXT,
    applied_skills  TEXT,
    error_message   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at    TIMESTAMP,
    FOREIGN KEY (character_id) REFERENCES characters(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
