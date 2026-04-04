-- Proxy definitions (provider-agnostic)
CREATE TABLE proxy_definition (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    proxy_id TEXT NOT NULL UNIQUE,
    display_name TEXT NOT NULL,
    provider_type TEXT NOT NULL,
    instance_id TEXT NOT NULL,
    provider_region TEXT,
    dns_subdomain TEXT NOT NULL,
    port INTEGER NOT NULL DEFAULT 16261,
    enabled INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT (datetime('now'))
);

-- Seed existing br-south proxy
INSERT INTO proxy_definition (proxy_id, display_name, provider_type, instance_id, provider_region, dns_subdomain, port)
VALUES ('br-south', 'SP1', 'AWS_EC2', 'i-022c5202d497b0ddd', 'sa-east-1', 'proxy-sp', 16261);
