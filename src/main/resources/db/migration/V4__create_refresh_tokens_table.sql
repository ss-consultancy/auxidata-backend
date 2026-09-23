CREATE TABLE IF NOT EXISTS refresh_token (
    id VARCHAR(512) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_expires ON refresh_token(expires_at);