-- Create refresh_tokens table for JWT token management
CREATE TABLE refresh_tokens (
    id BINARY(16) PRIMARY KEY,
    subject_id BINARY(16) NOT NULL,
    subject_type VARCHAR(20) NOT NULL DEFAULT 'USER',
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

-- Create indexes separately for H2 compatibility
CREATE INDEX idx_refresh_tokens_subject_id ON refresh_tokens (subject_id);
CREATE INDEX idx_refresh_tokens_subject_type ON refresh_tokens (subject_type);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_revoked ON refresh_tokens (revoked);
