-- =============================================================================
-- Seed: Payment Methods (8 métodos de pago)
-- =============================================================================


INSERT INTO payment_methods (id, code, name, description, is_active, created_at, updated_at)
VALUES
    (UNHEX('f9000000000000000000000000000001'), 'PM-0001', 'Efectivo',            'Pago en efectivo',                        TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000002'), 'PM-0002', 'Transferencia bancaria', 'Transferencia desde cuenta bancaria',  TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000003'), 'PM-0003', 'Tarjeta de débito',   'Pago con tarjeta de débito',              TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000004'), 'PM-0004', 'Tarjeta de crédito',  'Pago con tarjeta de crédito',             TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000005'), 'PM-0005', 'Cheque',              'Pago con cheque',                         TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000006'), 'PM-0006', 'Divisas',             'Pago en moneda extranjera (USD/EUR)',     TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000007'), 'PM-0007', 'Pago móvil',          'Pago móvil Venezuela',                    TRUE, NOW(), NOW()),
    (UNHEX('f9000000000000000000000000000008'), 'PM-0008', 'Zelle',               'Pago mediante Zelle',                     TRUE, NOW(), NOW());
