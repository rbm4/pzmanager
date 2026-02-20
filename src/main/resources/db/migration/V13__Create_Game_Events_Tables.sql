-- Game Events system: events, properties, and contributions

CREATE TABLE game_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    created_by_user_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    total_cost INTEGER NOT NULL,
    amount_collected INTEGER NOT NULL DEFAULT 0,
    duration_days INTEGER NOT NULL DEFAULT 7,
    activated_at TIMESTAMP,
    expiration_date TIMESTAMP,
    expired_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

CREATE TABLE game_event_properties (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_event_id INTEGER NOT NULL,
    suggestion_key TEXT NOT NULL,
    property_target TEXT NOT NULL,
    property_key TEXT NOT NULL,
    display_name TEXT NOT NULL,
    value_type TEXT NOT NULL,
    selected_value TEXT NOT NULL,
    calculated_delta TEXT,
    property_cost INTEGER NOT NULL,
    region_x1 INTEGER,
    region_x2 INTEGER,
    region_y1 INTEGER,
    region_y2 INTEGER,
    region_z INTEGER,
    linked_region_id INTEGER,
    FOREIGN KEY (game_event_id) REFERENCES game_events(id) ON DELETE CASCADE
);

CREATE TABLE game_event_contributions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    game_event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    contributed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_event_id) REFERENCES game_events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_game_events_status ON game_events(status);
CREATE INDEX idx_game_events_created_at ON game_events(created_at);
CREATE INDEX idx_game_event_contributions_event ON game_event_contributions(game_event_id);
CREATE INDEX idx_game_event_contributions_user ON game_event_contributions(user_id);
CREATE INDEX idx_game_event_properties_event ON game_event_properties(game_event_id);
