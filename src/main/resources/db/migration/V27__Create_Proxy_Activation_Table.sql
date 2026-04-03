CREATE TABLE proxy_activation (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id),
    proxy_id TEXT NOT NULL,
    instance_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'STARTING',
    credits_spent INTEGER NOT NULL,
    hours INTEGER NOT NULL,
    activated_at TIMESTAMP NOT NULL DEFAULT (datetime('now')),
    expires_at TIMESTAMP NOT NULL,
    stopped_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX idx_proxy_activation_expiry ON proxy_activation(status, expires_at);
CREATE INDEX idx_proxy_activation_user ON proxy_activation(user_id);
