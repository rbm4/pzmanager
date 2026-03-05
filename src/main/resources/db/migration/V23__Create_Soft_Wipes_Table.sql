CREATE TABLE IF NOT EXISTS soft_wipes (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL REFERENCES users(id),
    status      TEXT    NOT NULL DEFAULT 'WAITING_RESTART',
    x1          INTEGER NOT NULL,
    y1          INTEGER NOT NULL,
    x2          INTEGER NOT NULL,
    y2          INTEGER NOT NULL,
    cost        INTEGER NOT NULL,
    created_at  TEXT    NOT NULL,
    executed_at TEXT,
    bins_deleted   INTEGER DEFAULT 0,
    bins_protected INTEGER DEFAULT 0,
    error_message  TEXT
);

CREATE INDEX idx_soft_wipes_status ON soft_wipes(status);
CREATE INDEX idx_soft_wipes_user_id ON soft_wipes(user_id);
