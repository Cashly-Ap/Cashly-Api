-- =============================================================================
-- Parsun API - Seed Data
-- Run manually: docker compose exec -T mysql mysql -umyuser -psecret mydatabase < db/seed.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- USERS
-- Password for all users: 49510561Gabriel.
-- BCrypt hash: $2a$10$7DziWVMHsxgZGPCH5yVatuBltVfIO6V60fIqaNUvzHJMRvS7OZfhy
-- -----------------------------------------------------------------------------
INSERT INTO users (id, email, user_name, password_hash, role, active, email_verified, commission_deduction_percentage, created_at, updated_at)
VALUES
    (UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380001'), 'gabriel@gmail.com', 'Gabriel', '$2a$10$7DziWVMHsxgZGPCH5yVatuBltVfIO6V60fIqaNUvzHJMRvS7OZfhy', 'SUPER_ADMIN', TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380002'), 'admin@parsun.com',  'Admin',   '$2a$10$7DziWVMHsxgZGPCH5yVatuBltVfIO6V60fIqaNUvzHJMRvS7OZfhy', 'ADMIN',       TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380003'), 'op@parsun.com',     'Operador','$2a$10$7DziWVMHsxgZGPCH5yVatuBltVfIO6V60fIqaNUvzHJMRvS7OZfhy', 'OPERATOR',    TRUE, TRUE, 1.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE email = email;

-- -----------------------------------------------------------------------------
-- CUSTOMERS
-- -----------------------------------------------------------------------------
INSERT INTO customers (id, code, legal_name, type, rif, alias, credit_days, credit_limit, active, created_at, updated_at)
VALUES
    (UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380001'), 1, 'Distribuidora Caracas C.A.',   'BUSINESS', 'J-30123456-7', 'DistCaracas', 30, 5000.00,  TRUE, NOW(), NOW()),
    (UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380002'), 2, 'Importaciones del Sur C.A.',   'BUSINESS', 'J-20987654-3', 'ImpSur',      60, 10000.00, TRUE, NOW(), NOW()),
    (UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380003'), 3, 'Comercializadora Norte S.R.L.','BUSINESS', 'J-10555444-1', 'ComNorte',    15, 2500.00,  TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE code = code;

-- -----------------------------------------------------------------------------
-- CUSTOMER EMAILS
-- -----------------------------------------------------------------------------
INSERT INTO customer_emails (id, customer_id, email)
VALUES
    (UNHEX('c0eebc999c0b4ef8bb6d6bb9bd380001'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380001'), 'contacto@distcaracas.com'),
    (UNHEX('c0eebc999c0b4ef8bb6d6bb9bd380002'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380002'), 'ventas@impsur.com'),
    (UNHEX('c0eebc999c0b4ef8bb6d6bb9bd380003'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380003'), 'info@comnorte.com')
ON DUPLICATE KEY UPDATE email = email;

-- -----------------------------------------------------------------------------
-- CUSTOMER PHONES
-- -----------------------------------------------------------------------------
INSERT INTO customer_phones (id, customer_id, phone)
VALUES
    (UNHEX('d0eebc999c0b4ef8bb6d6bb9bd380001'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380001'), '+58-212-5551234'),
    (UNHEX('d0eebc999c0b4ef8bb6d6bb9bd380002'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380002'), '+58-212-5555678'),
    (UNHEX('d0eebc999c0b4ef8bb6d6bb9bd380003'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380003'), '+58-261-5559012')
ON DUPLICATE KEY UPDATE phone = phone;

-- -----------------------------------------------------------------------------
-- CUSTOMER ADDRESSES
-- -----------------------------------------------------------------------------
INSERT INTO customer_addresses (id, customer_id, address, billing, active)
VALUES
    (UNHEX('e0eebc999c0b4ef8bb6d6bb9bd380001'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380001'), 'Av. Libertador, Edificio Centro, Piso 5, Caracas', TRUE, TRUE),
    (UNHEX('e0eebc999c0b4ef8bb6d6bb9bd380002'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380002'), 'Calle 10, Local 3, Maracaibo',                     TRUE, TRUE),
    (UNHEX('e0eebc999c0b4ef8bb6d6bb9bd380003'), UNHEX('b0eebc999c0b4ef8bb6d6bb9bd380003'), 'Carrera 5, Galpón Industrial, Barquisimeto',        TRUE, TRUE)
ON DUPLICATE KEY UPDATE address = address;
