-- Seed admin user for Cashly API
-- Password: 49510561Gabriel. (BCrypt hash)
-- Role: SUPER_ADMIN
INSERT INTO users (id, email, user_name, password_hash, role, active, email_verified, commission_deduction_percentage, created_at, updated_at)
VALUES (UNHEX(REPLACE('a0eebc999c0b4ef8bb6d6bb9bd20a215', '-', '')), 'gabriel@gmail.com', 'Gabriel', '$2a$10$7DziWVMHsxgZGPCH5yVatuBltVfIO6V60fIqaNUvzHJMRvS7OZfhy', 'SUPER_ADMIN', TRUE, TRUE, 1.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = email;