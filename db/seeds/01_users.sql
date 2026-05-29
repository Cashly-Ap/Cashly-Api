-- =============================================================================
-- Seed: Users & Permissions
-- Password for all: 49510561Gabriel.
-- Hash (all): $2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy
-- =============================================================================

DELETE FROM user_module_permissions;
DELETE FROM refresh_tokens;
DELETE FROM users;

INSERT INTO users (id, email, user_name, password_hash, role, active, email_verified, commission_deduction_percentage, created_at, updated_at)
VALUES
    (UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'gabriel@gmail.com',     'Gabriel',       '$2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy', 'SUPER_ADMIN', TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'superadmin@parsun.com', 'Super Admin',   '$2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy', 'SUPER_ADMIN', TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'admin@parsun.com',      'Admin User',    '$2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy', 'ADMIN',       TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), 'operator@parsun.com',   'Operator User', '$2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy', 'OPERATOR',    TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('d3bbef222f3e5cb1ee9f9eecf053d548'), 'sales@parsun.com',      'Sales User',    '$2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy', 'OPERATOR',    TRUE, TRUE, 1.00, NOW(), NOW()),
    (UNHEX('e4ccf0333a4f6dc2ff0a0ffd0164e659'), 'nosales@parsun.com',    'No Sales User', '$2a$10$v9GJVVjLt5GrhSdy6ITNn.Tagp/2D6ul1ctfZ3MLTufbNAq/TLefy', 'OPERATOR',    TRUE, TRUE, 1.00, NOW(), NOW());

INSERT INTO user_module_permissions (id, user_id, module)
VALUES
    (UNHEX('aa000000000000000000000000000001'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'USERS'),
    (UNHEX('aa000000000000000000000000000002'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'INVENTORY'),
    (UNHEX('aa000000000000000000000000000003'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'SALES'),
    (UNHEX('aa000000000000000000000000000004'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'FINANCES'),
    (UNHEX('aa000000000000000000000000000005'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'PURCHASES'),
    (UNHEX('aa000000000000000000000000000006'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'CUSTOMER_PORTAL'),
    (UNHEX('aa000000000000000000000000000007'), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'DOCUMENT_ADMIN'),

    (UNHEX('bb000000000000000000000000000001'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'USERS'),
    (UNHEX('bb000000000000000000000000000002'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'INVENTORY'),
    (UNHEX('bb000000000000000000000000000003'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'SALES'),
    (UNHEX('bb000000000000000000000000000004'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'FINANCES'),
    (UNHEX('bb000000000000000000000000000005'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'PURCHASES'),
    (UNHEX('bb000000000000000000000000000006'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'CUSTOMER_PORTAL'),
    (UNHEX('bb000000000000000000000000000007'), UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'DOCUMENT_ADMIN'),

    (UNHEX('cc000000000000000000000000000001'), UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'USERS'),
    (UNHEX('cc000000000000000000000000000002'), UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'INVENTORY'),
    (UNHEX('cc000000000000000000000000000003'), UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'SALES'),
    (UNHEX('cc000000000000000000000000000004'), UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'FINANCES'),
    (UNHEX('cc000000000000000000000000000005'), UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'PURCHASES'),
    (UNHEX('cc000000000000000000000000000006'), UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'DOCUMENT_ADMIN'),

    (UNHEX('dd000000000000000000000000000001'), UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), 'INVENTORY'),
    (UNHEX('dd000000000000000000000000000002'), UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), 'SALES'),
    (UNHEX('dd000000000000000000000000000003'), UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), 'PURCHASES'),

    (UNHEX('ee000000000000000000000000000001'), UNHEX('d3bbef222f3e5cb1ee9f9eecf053d548'), 'SALES'),

    (UNHEX('ff000000000000000000000000000001'), UNHEX('e4ccf0333a4f6dc2ff0a0ffd0164e659'), 'INVENTORY'),
    (UNHEX('ff000000000000000000000000000002'), UNHEX('e4ccf0333a4f6dc2ff0a0ffd0164e659'), 'PURCHASES');
