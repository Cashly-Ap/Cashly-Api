-- =============================================================================
-- Seed: Payment Methods (8 métodos de pago)
-- =============================================================================

DELETE FROM payment_methods;

INSERT INTO payment_methods (id, code, name, description, is_active, created_at, updated_at)
VALUES
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0001', 'Efectivo',            'Pago en efectivo',                        TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0002', 'Transferencia bancaria', 'Transferencia desde cuenta bancaria',  TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0003', 'Tarjeta de débito',   'Pago con tarjeta de débito',              TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0004', 'Tarjeta de crédito',  'Pago con tarjeta de crédito',             TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0005', 'Cheque',              'Pago con cheque',                         TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0006', 'Divisas',             'Pago en moneda extranjera (USD/EUR)',     TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0007', 'Pago móvil',          'Pago móvil Venezuela',                    TRUE, NOW(), NOW()),
    (UNHEX(REPLACE(UUID(), '-', '')), 'PM-0008', 'Zelle',               'Pago mediante Zelle',                     TRUE, NOW(), NOW());
