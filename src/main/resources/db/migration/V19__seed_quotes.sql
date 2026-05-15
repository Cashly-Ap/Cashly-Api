-- Seed data for quotes with diverse statuses and customers
-- Uses existing customers, products, warehouses, and users

-- Quote 1: DRAFT - DistCaracas (multi-line)
INSERT INTO quotes (id, quote_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (UNHEX('Q1000000000000000000000000000001'), 'PRE-2026-0007', UNHEX('C1000000000000000000000000000001'), 'Distribuidora Caracas C.A.', 'J-30123456-7', UNHEX('A0EEBC999C0B4EF8BB6D6BB9BD380215'), 'Super Admin', 'DRAFT', 'Presupuesto para proyecto de cableado', 4250.00, NOW(), NOW());

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000001'), UNHEX('Q1000000000000000000000000000001'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 500.0000, 5.50, 2750.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000002'), UNHEX('Q1000000000000000000000000000001'), UNHEX('E0000000000000000000000000000002'), 'Toma corriente doble', 'ELEC-002', 100.0000, 14.00, 1400.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000003'), UNHEX('Q1000000000000000000000000000001'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 2.0000, 50.00, 100.00);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000001'), UNHEX('Q1000000000000000000000000000001'), UNHEX('Q2000000000000000000000000000001'), UNHEX('E0000000000000000000000000000001'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 500.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000002'), UNHEX('Q1000000000000000000000000000001'), UNHEX('Q2000000000000000000000000000002'), UNHEX('E0000000000000000000000000000002'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 100.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000003'), UNHEX('Q1000000000000000000000000000001'), UNHEX('Q2000000000000000000000000000003'), UNHEX('E0000000000000000000000000000003'), UNHEX('B0000000000000000000000000000002'), 'Almacen Maracaibo Norte', 2.0000);

-- Quote 2: DRAFT - ImpSur (single line)
INSERT INTO quotes (id, quote_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (UNHEX('Q1000000000000000000000000000002'), 'PRE-2026-0008', UNHEX('C1000000000000000000000000000002'), 'Importaciones del Sur', 'J-20987654-3', UNHEX('B1FFCD000D1C4FA9BC7E7CCACE31B326'), 'Admin User', 'DRAFT', NULL, 1050.00, NOW(), NOW());

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000004'), UNHEX('Q1000000000000000000000000000002'), UNHEX('E0000000000000000000000000000004'), 'Multimetro digital', 'ELEC-004', 5.0000, 210.00, 1050.00);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000004'), UNHEX('Q1000000000000000000000000000002'), UNHEX('Q2000000000000000000000000000004'), UNHEX('E0000000000000000000000000000004'), UNHEX('B0000000000000000000000000000002'), 'Almacen Maracaibo Norte', 5.0000);

-- Quote 3: CONVERTED_TO_ORDER - ComNorte
INSERT INTO quotes (id, quote_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, notes, subtotal, order_id, order_number, created_at, updated_at)
VALUES (UNHEX('Q1000000000000000000000000000003'), 'PRE-2026-0009', UNHEX('C1000000000000000000000000000003'), 'Comercial del Norte', 'J-10555444-1', UNHEX('C2AADE111E2D4BA0DD8F8DDBDF42C437'), 'Operator User', 'CONVERTED_TO_ORDER', 'Convertido a pedido', 6300.00, UNHEX('O1000000000000000000000000000001'), 'ORD-2026-0001', NOW(), NOW());

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000005'), UNHEX('Q1000000000000000000000000000003'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 10.0000, 350.00, 3500.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000006'), UNHEX('Q1000000000000000000000000000003'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 200.0000, 5.50, 1100.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000007'), UNHEX('Q1000000000000000000000000000003'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 38.0000, 45.00, 1710.00);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000005'), UNHEX('Q1000000000000000000000000000003'), UNHEX('Q2000000000000000000000000000005'), UNHEX('E0000000000000000000000000000005'), UNHEX('B0000000000000000000000000000003'), 'Almacen Valencia', 10.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000006'), UNHEX('Q1000000000000000000000000000003'), UNHEX('Q2000000000000000000000000000006'), UNHEX('E0000000000000000000000000000001'), UNHEX('B0000000000000000000000000000003'), 'Almacen Valencia', 200.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000007'), UNHEX('Q1000000000000000000000000000003'), UNHEX('Q2000000000000000000000000000007'), UNHEX('E0000000000000000000000000000003'), UNHEX('B0000000000000000000000000000003'), 'Almacen Valencia', 38.0000);

-- Quote 4: CONVERTED_TO_INVOICE - CMendoza
INSERT INTO quotes (id, quote_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, notes, subtotal, invoice_id, invoice_number, created_at, updated_at)
VALUES (UNHEX('Q1000000000000000000000000000004'), 'PRE-2026-0010', UNHEX('C1000000000000000000000000000004'), 'Carlos Mendoza', 'V-12345678-9', UNHEX('D3BBEF222F3E5CB1EE9F9EECF053D548'), 'Sales User', 'CONVERTED_TO_INVOICE', 'Convertido a factura pago contado', 875.00, UNHEX('F1000000000000000000000000000001'), 'INV-2026-00026', NOW(), NOW());

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000008'), UNHEX('Q1000000000000000000000000000004'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 5.0000, 175.00, 875.00);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000008'), UNHEX('Q1000000000000000000000000000004'), UNHEX('Q2000000000000000000000000000008'), UNHEX('E0000000000000000000000000000005'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 5.0000);

-- Quote 5: DRAFT - SumZulia (multi-line, multi-warehouse)
INSERT INTO quotes (id, quote_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (UNHEX('Q1000000000000000000000000000005'), 'PRE-2026-0011', UNHEX('C1000000000000000000000000000005'), 'Suministros Zulia', 'J-40234567-8', UNHEX('F5DDEA444B5A7ED3CC1B1AAED175F76A'), 'Gabriel', 'DRAFT', 'Presupuesto para remodelacion de oficina', 12450.00, NOW(), NOW());

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000009'), UNHEX('Q1000000000000000000000000000005'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 800.0000, 5.50, 4400.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000010'), UNHEX('Q1000000000000000000000000000005'), UNHEX('E0000000000000000000000000000002'), 'Toma corriente doble', 'ELEC-002', 200.0000, 14.00, 2800.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000011'), UNHEX('Q1000000000000000000000000000005'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 60.0000, 45.00, 2700.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000012'), UNHEX('Q1000000000000000000000000000005'), UNHEX('E0000000000000000000000000000004'), 'Multimetro digital', 'ELEC-004', 3.0000, 210.00, 630.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000013'), UNHEX('Q1000000000000000000000000000005'), UNHEX('E0000000000000000000000000000005'), 'Taladro percutor 13mm', 'HERR-001', 4.0000, 350.00, 1400.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000014'), UNHEX('Q1000000000000000000000000000005'), UNHEX('E0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)', 'ELEC-001', 94.0000, 5.50, 517.00);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000009'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000009'), UNHEX('E0000000000000000000000000000001'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 500.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000010'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000009'), UNHEX('E0000000000000000000000000000001'), UNHEX('B0000000000000000000000000000002'), 'Almacen Maracaibo Norte', 300.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000011'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000010'), UNHEX('E0000000000000000000000000000002'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 200.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000012'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000011'), UNHEX('E0000000000000000000000000000003'), UNHEX('B0000000000000000000000000000003'), 'Almacen Valencia', 60.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000013'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000012'), UNHEX('E0000000000000000000000000000004'), UNHEX('B0000000000000000000000000000002'), 'Almacen Maracaibo Norte', 3.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000014'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000013'), UNHEX('E0000000000000000000000000000005'), UNHEX('B0000000000000000000000000000004'), 'Almacen Barquisimeto', 4.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000015'), UNHEX('Q1000000000000000000000000000005'), UNHEX('Q2000000000000000000000000000014'), UNHEX('E0000000000000000000000000000001'), UNHEX('B0000000000000000000000000000004'), 'Almacen Barquisimeto', 94.0000);

-- Quote 6: DRAFT - DistCaracas (small quote)
INSERT INTO quotes (id, quote_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (UNHEX('Q1000000000000000000000000000006'), 'PRE-2026-0012', UNHEX('C1000000000000000000000000000001'), 'Distribuidora Caracas C.A.', 'J-30123456-7', UNHEX('A0EEBC999C0B4EF8BB6D6BB9BD380215'), 'Super Admin', 'DRAFT', 'Reposicion de material electrico', 420.00, NOW(), NOW());

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000015'), UNHEX('Q1000000000000000000000000000006'), UNHEX('E0000000000000000000000000000003'), 'Breaker 20A', 'ELEC-003', 8.0000, 45.00, 360.00);

INSERT INTO quote_lines (id, quote_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES (UNHEX('Q2000000000000000000000000000016'), UNHEX('Q1000000000000000000000000000006'), UNHEX('E0000000000000000000000000000002'), 'Toma corriente doble', 'ELEC-002', 4.0000, 15.00, 60.00);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000016'), UNHEX('Q1000000000000000000000000000006'), UNHEX('Q2000000000000000000000000000015'), UNHEX('E0000000000000000000000000000003'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 8.0000);

INSERT INTO quote_allocations (id, quote_id, quote_line_id, product_id, warehouse_id, warehouse_name, allocated_quantity)
VALUES (UNHEX('Q3000000000000000000000000000017'), UNHEX('Q1000000000000000000000000000006'), UNHEX('Q2000000000000000000000000000016'), UNHEX('E0000000000000000000000000000002'), UNHEX('B0000000000000000000000000000001'), 'Almacen Principal Caracas', 4.0000);

-- Update sequence
UPDATE quote_number_sequence SET next_value = 13 WHERE year = 2026;
