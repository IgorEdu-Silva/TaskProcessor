CREATE TABLE IF NOT EXISTS task (
    id UUID PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payload TEXT,
    retry_count INT DEFAULT 0,
    created_at TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    next_retry_at TIMESTAMP
);