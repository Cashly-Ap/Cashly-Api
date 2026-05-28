INSERT INTO credit_notes (id, note_number, customer_id, invoice_id, amount, reason, created_by, created_by_name, created_at, updated_at)
VALUES
    (UNHEX('da000000000000000000000000000001'), 'CN-2025-0001', (SELECT id FROM customers WHERE code = 1001), (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00026'), 500.00, 'Devolucion de productos', 'APPLIED', 500.00, (SELECT id FROM users WHERE email = 'superadmin@parsun.com'), 'Super Admin', NOW(), NOW()),
    (UNHEX('da000000000000000000000000000002'), 'CN-2025-0002', (SELECT id FROM customers WHERE code = 1002), (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00027'), 200.00, 'Descuento por defecto de fabrica', 'APPLIED', 200.00, (SELECT id FROM users WHERE email = 'admin@parsun.com'), 'Admin User', NOW(), NOW()),
    (UNHEX('da000000000000000000000000000003'), 'CN-2025-0003', (SELECT id FROM customers WHERE code = 1003), (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00028'), 150.00, 'Correccion de precio', 'DRAFT', 0.00, (SELECT id FROM users WHERE email = 'operator@parsun.com'), 'Operator User', NOW(), NOW());

INSERT INTO credit_note_number_sequence (year, next_value)
VALUES (2025, 4);

INSERT INTO debit_notes (id, note_number, customer_id, invoice_id, amount, reason, created_by, created_by_name, created_at, updated_at)
VALUES
    (UNHEX('db000000000000000000000000000001'), 'DN-2025-0001', (SELECT id FROM customers WHERE code = 1004), (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00029'), 75.00, 'Recargo por pago tardio', 'APPLIED', 75.00, (SELECT id FROM users WHERE email = 'sales@parsun.com'), 'Sales User', NOW(), NOW()),
    (UNHEX('db000000000000000000000000000002'), 'DN-2025-0002', (SELECT id FROM customers WHERE code = 1005), (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00030'), 120.00, 'Intereses por credito vencido', 'APPLIED', 120.00, (SELECT id FROM users WHERE email = 'superadmin@parsun.com'), 'Super Admin', NOW(), NOW()),
    (UNHEX('db000000000000000000000000000003'), 'DN-2025-0003', (SELECT id FROM customers WHERE code = 1007), (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00026'), 50.00, 'Ajuste de precio', 'DRAFT', 0.00, (SELECT id FROM users WHERE email = 'admin@parsun.com'), 'Admin User', NOW(), NOW());

INSERT INTO debit_note_number_sequence (year, next_value)
VALUES (2025, 4);
