CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    user_name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'OPERATOR',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    commission_deduction_percentage DECIMAL(5,2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_active ON users (active);
CREATE INDEX idx_users_role ON users (role);

CREATE TABLE user_module_permissions (
    id BINARY(16) PRIMARY KEY,
    user_id BINARY(16) NOT NULL,
    module VARCHAR(30) NOT NULL,
    CONSTRAINT fk_ump_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_ump_user_module UNIQUE (user_id, module)
);

CREATE INDEX idx_ump_user_id ON user_module_permissions (user_id);
