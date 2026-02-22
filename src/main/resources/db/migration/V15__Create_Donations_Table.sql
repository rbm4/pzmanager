CREATE TABLE IF NOT EXISTS donations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    pagbank_order_id TEXT,
    amount_centavos INTEGER NOT NULL,
    coins_awarded INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    pix_copy_paste TEXT,
    qr_code_image_url TEXT,
    expires_at TEXT NOT NULL,
    paid_at TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);
