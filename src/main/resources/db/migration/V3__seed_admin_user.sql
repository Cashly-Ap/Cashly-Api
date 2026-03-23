-- Seed admin user
-- Email: Parsun.ve.alexandre@gmail.com
-- Password: 12345678Alex.
INSERT INTO users (id, email, user_name, password_hash, active, email_verified, commission_deduction_percentage, created_at, updated_at)
VALUES (
    UNHEX(REPLACE(UUID(), '-', '')),
    'parsun.ve.alexandre@gmail.com',
    'Alexandre',
    '$2b$10$DdyVsk.GHePtz5f1ZaKExeAsU5PpagPCq3GrCKQJm0GyeTUGKXbkS',
    TRUE,
    TRUE,
    1.00,
    NOW(),
    NOW()
);
