INSERT IGNORE INTO companies (id, singleton_key, legal_name, trade_name,
    tax_id, email, phone, website, street, city, region, country,
    postal_code)
VALUES (0x92000000000000000000000000000001, 1, 'Cashly Demo, C.A.',
    'Cashly Demo', 'J-00000000-0', 'hello@cashly.local',
    '+58-212-0000000', 'https://cashly.local', 'Avenida Principal',
    'Caracas', 'Distrito Capital', 'Venezuela', '1010');

INSERT IGNORE INTO auth_roles (id, name, description, active, version,
    system_key)
VALUES (0x90000000000000000000000000000001, 'CI system administrator',
    'Base role for integration tests', 1, 0, 'SYSTEM_ADMIN'),
    (0x90000000000000000000000000000003, 'CI operator',
    'Restricted role for integration tests', 1, 0, 'OPERATOR');

INSERT IGNORE INTO auth_departments (id, name, description, active, version)
VALUES (0x91000000000000000000000000000001, 'CI administration',
    'Base department for integration tests', 1, 0),
    (0x91000000000000000000000000000002, 'CI sales',
    'Sales department for integration tests', 1, 0);

INSERT IGNORE INTO auth_administration_lock (id) VALUES (1);
