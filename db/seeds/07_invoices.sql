-- Seed data for invoices with diverse statuses and customers
-- Uses existing customers, products, warehouses, and users

-- Invoice 1: DRAFT - CASH - DistCaracas
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000001'), 'INV-2026-00026', NULL, NULL, UNHEX('C1000000000000000000000000000001'), 'Distribuidora Caracas C.A.', 'J-30123456-7', UNHEX('A0EEBC999C0B4EF8BB6D6BB9BD380215'), 'Super Admin', 'DRAFT', 'CASH', 'Factura borrador para prueba', 1250.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000001')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000001'), UNHEX('0F100000000000000000000000000001'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 100.0000, 5.50, 550.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000002'), UNHEX('0F100000000000000000000000000001'), UNHEX('E0000000000000000000000000000002'), 'Toma corriente doble', 'ELEC-002', 50.0000, 14.00, 700.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000001'), UNHEX('0F200000000000000000000000000001'), UNHEX('B0000000000000000000000000000001'), 100.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000002'), UNHEX('0F200000000000000000000000000002'), UNHEX('B0000000000000000000000000000001'), 50.0000);

-- Invoice 2: PENDING_PAYMENT - CREDIT - ImpSur (payment cancelled due to return)
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000002'), 'INV-2026-00027', NULL, NULL, UNHEX('C1000000000000000000000000000002'), 'Importaciones del Sur', 'J-20987654-3', UNHEX('B1FFCD000D1C4FA9BC7E7CCACE31B326'), 'Admin User', 'PENDING_PAYMENT', 'CREDIT', 'Pago a 30 dias - cancelado por devolucion', 3450.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000002')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000003'), UNHEX('0F100000000000000000000000000002'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 30.0000, 45.00, 1350.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000004'), UNHEX('0F100000000000000000000000000002'), UNHEX('E0000000000000000000000000000004'), 'Multimetro digital', 'ELEC-004', 10.0000, 210.00, 2100.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000003'), UNHEX('0F200000000000000000000000000003'), UNHEX('B0000000000000000000000000000002'), 30.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000004'), UNHEX('0F200000000000000000000000000004'), UNHEX('B0000000000000000000000000000002'), 10.0000);

-- Invoice 3: PENDING_PAYMENT - CREDIT - ComNorte
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000003'), 'INV-2026-00028', NULL, NULL, UNHEX('C1000000000000000000000000000003'), 'Comercial del Norte', 'J-10555444-1', UNHEX('C2AADE111E2D4BA0DD8F8DDBDF42C437'), 'Operator User', 'PENDING_PAYMENT', 'CREDIT', 'Pendiente de pago - credito 60 dias', 8850.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000003')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000005'), UNHEX('0F100000000000000000000000000003'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 5.0000, 350.00, 1750.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000006'), UNHEX('0F100000000000000000000000000003'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 500.0000, 5.50, 2750.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000007'), UNHEX('0F100000000000000000000000000003'), UNHEX('E0000000000000000000000000000002'), 'Toma corriente doble', 'ELEC-002', 150.0000, 14.00, 2100.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000008'), UNHEX('0F100000000000000000000000000003'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 50.0000, 45.00, 2250.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000005'), UNHEX('0F200000000000000000000000000005'), UNHEX('B0000000000000000000000000000003'), 5.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000006'), UNHEX('0F200000000000000000000000000006'), UNHEX('B0000000000000000000000000000003'), 500.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000007'), UNHEX('0F200000000000000000000000000007'), UNHEX('B0000000000000000000000000000003'), 150.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000008'), UNHEX('0F200000000000000000000000000008'), UNHEX('B0000000000000000000000000000003'), 50.0000);

-- Invoice 4: DRAFT - CASH - CMendoza (single line)
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000004'), 'INV-2026-00029', NULL, NULL, UNHEX('C1000000000000000000000000000004'), 'Carlos Mendoza', 'V-12345678-9', UNHEX('D3BBEF222F3E5CB1EE9F9EECF053D548'), 'Sales User', 'DRAFT', 'CASH', NULL, 175.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000004')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000009'), UNHEX('0F100000000000000000000000000004'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 1.0000, 175.00, 175.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000009'), UNHEX('0F200000000000000000000000000009'), UNHEX('B0000000000000000000000000000001'), 1.0000);

-- Invoice 5: PARTIALLY_PAID - CREDIT - SumZulia (multi-line, multi-warehouse)
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000005'), 'INV-2026-00030', NULL, NULL, UNHEX('C1000000000000000000000000000005'), 'Suministros Zulia', 'J-40234567-8', UNHEX('F5DDEA444B5A7ED3CC1B1AAED175F76A'), 'Gabriel', 'PARTIALLY_PAID', 'CREDIT', 'Credito vencido - intereses generados', 15600.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000005')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000010'), UNHEX('0F100000000000000000000000000005'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 1000.0000, 5.50, 5500.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000011'), UNHEX('0F100000000000000000000000000005'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 100.0000, 45.00, 4500.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000012'), UNHEX('0F100000000000000000000000000005'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 8.0000, 350.00, 2800.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000013'), UNHEX('0F100000000000000000000000000005'), UNHEX('E0000000000000000000000000000004'), 'Multimetro digital', 'ELEC-004', 4.0000, 210.00, 840.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000014'), UNHEX('0F100000000000000000000000000005'), UNHEX('E0000000000000000000000000000002'), 'Toma corriente doble', 'ELEC-002', 140.0000, 14.00, 1960.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000010'), UNHEX('0F200000000000000000000000000010'), UNHEX('B0000000000000000000000000000001'), 600.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000011'), UNHEX('0F200000000000000000000000000010'), UNHEX('B0000000000000000000000000000002'), 400.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000012'), UNHEX('0F200000000000000000000000000011'), UNHEX('B0000000000000000000000000000001'), 100.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000013'), UNHEX('0F200000000000000000000000000012'), UNHEX('B0000000000000000000000000000003'), 8.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000014'), UNHEX('0F200000000000000000000000000013'), UNHEX('B0000000000000000000000000000002'), 4.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000015'), UNHEX('0F200000000000000000000000000014'), UNHEX('B0000000000000000000000000000001'), 140.0000);

-- Update sequence
UPDATE invoice_number_sequence SET next_value = 31 WHERE year = 2026;

-- Invoice 6: PARTIALLY_PAID - CREDIT - InvGlobal (linked to ORD-2026-0008)
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000006'), 'INV-2026-00031', UNHEX('aa000000000000000000000000000008'), 'ORD-2026-0008', UNHEX('C1000000000000000000000000000011'), 'Inversiones Global S.A.', 'J-99999999-9', UNHEX('A0EEBC999C0B4EF8BB6D6BB9BD380215'), 'Super Admin', 'PARTIALLY_PAID', 'CREDIT', 'Credito 30 dias - pago parcial recibido', 1000.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000011')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000015'), UNHEX('0F100000000000000000000000000006'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 100.0000, 5.50, 550.00);

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000016'), UNHEX('0F100000000000000000000000000006'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 10.0000, 45.00, 450.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000016'), UNHEX('0F200000000000000000000000000015'), UNHEX('B0000000000000000000000000000001'), 100.0000);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000017'), UNHEX('0F200000000000000000000000000016'), UNHEX('B0000000000000000000000000000001'), 10.0000);

-- Invoice 7: PAID - CASH - InvGlobal (single line)
INSERT INTO invoices (id, invoice_number, order_id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, payment_type, notes, subtotal, created_at, updated_at, issue_date, due_date)
VALUES (UNHEX('0F100000000000000000000000000007'), 'INV-2026-00032', NULL, NULL, UNHEX('C1000000000000000000000000000011'), 'Inversiones Global S.A.', 'J-99999999-9', UNHEX('A0EEBC999C0B4EF8BB6D6BB9BD380215'), 'Super Admin', 'PAID', 'CASH', 'Pago de contado', 350.00, NOW(), NOW(), CURRENT_DATE(), (SELECT DATE_ADD(CURRENT_DATE(), INTERVAL COALESCE(credit_days, 0) DAY) FROM customers WHERE id = UNHEX('C1000000000000000000000000000011')));

INSERT INTO invoice_lines (id, invoice_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('0F200000000000000000000000000017'), UNHEX('0F100000000000000000000000000007'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 2.0000, 175.00, 350.00);

INSERT INTO invoice_line_allocations (id, invoice_line_id, warehouse_id, allocated)
VALUES (UNHEX('0F300000000000000000000000000018'), UNHEX('0F200000000000000000000000000017'), UNHEX('B0000000000000000000000000000001'), 2.0000);

-- Update sequence
UPDATE invoice_number_sequence SET next_value = 33 WHERE year = 2026;

INSERT INTO invoice_number_sequence (year,next_value) VALUES (2026,33);
