CREATE TABLE safehouse_claim_requests (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    claim_name VARCHAR(120) NOT NULL,
    x1 INTEGER NOT NULL,
    y1 INTEGER NOT NULL,
    x2 INTEGER NOT NULL,
    y2 INTEGER NOT NULL,
    cost INTEGER NOT NULL,
    overlaps_existing BOOLEAN NOT NULL DEFAULT 0,
    overlap_count INTEGER NOT NULL DEFAULT 0,
    admin_reason TEXT,
    reviewed_by VARCHAR(100),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_safehouse_claim_requests_user_id ON safehouse_claim_requests(user_id);
CREATE INDEX idx_safehouse_claim_requests_status ON safehouse_claim_requests(status);