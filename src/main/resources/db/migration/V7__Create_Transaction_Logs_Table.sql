-- Create transaction_logs table
CREATE TABLE IF NOT EXISTS transaction_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    transaction_type TEXT NOT NULL,
    item_name TEXT,
    item_id_ref TEXT,
    amount INTEGER NOT NULL,
    balance_after INTEGER NOT NULL,
    character_name TEXT NOT NULL,
    player_username TEXT NOT NULL,
    cashback BOOLEAN NOT NULL DEFAULT 0,
    cashback_at TIMESTAMP,
    cashback_by TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);
