CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY,
    language VARCHAR(16) NOT NULL,
    current_node_id VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_active_at TIMESTAMP NOT NULL
);
