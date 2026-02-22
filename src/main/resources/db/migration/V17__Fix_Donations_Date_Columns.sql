-- Fix donations date columns: TEXT -> TIMESTAMP to match Hibernate's epoch millis storage
-- (aligns with other tables like game_events, transaction_logs, sandbox_settings)
CREATE TABLE donations_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    character_id INTEGER NOT NULL,
    pagbank_order_id TEXT,
    amount_centavos INTEGER NOT NULL,
    coins_awarded INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    pix_copy_paste TEXT,
    qr_code_image_url TEXT,
    expires_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

INSERT INTO donations_new SELECT * FROM donations;
DROP TABLE donations;
ALTER TABLE donations_new RENAME TO donations;
