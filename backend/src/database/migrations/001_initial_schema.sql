-- LiveCaster Database Schema
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role VARCHAR(32) DEFAULT 'PRODUCER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS broadcasts (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    rtmp_url VARCHAR(512) NOT NULL,
    stream_key VARCHAR(255) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    status VARCHAR(32) DEFAULT 'DRAFT',
    duration_seconds INT DEFAULT 0,
    peak_viewers INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS destinations (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    platform VARCHAR(32) NOT NULL,
    rtmp_url VARCHAR(512) NOT NULL,
    stream_key VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
