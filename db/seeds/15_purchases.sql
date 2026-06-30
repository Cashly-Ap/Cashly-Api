-- =============================================================================
-- Seed: Purchases (Compras)
-- 6 purchases with diverse statuses, suppliers, products, and warehouses
-- Uses existing suppliers (data.sql), products (05_products.sql), warehouses (04_warehouses.sql), users (01_users.sql)
-- =============================================================================

-- Suppliers (from data.sql)
SET @sup1 = UNHEX('a1000000000000000000000000000001');
SET @sup2 = UNHEX('a2000000000000000000000000000002');
SET @sup3 = UNHEX('a3000000000000000000000000000003');

-- Products (from 05_products.sql)
SET @prod1  = UNHEX('E0000000000000000000000000000001');
SET @prod2  = UNHEX('E0000000000000000000000000000002');
SET @prod3  = UNHEX('E0000000000000000000000000000003');
SET @prod4  = UNHEX('E0000000000000000000000000000004');
SET @prod5  = UNHEX('E0000000000000000000000000000005');
SET @prod6  = UNHEX('E0000000000000000000000000000006');
SET @prod7  = UNHEX('E0000000000000000000000000000007');
SET @prod8  = UNHEX('E0000000000000000000000000000008');
SET @prod9  = UNHEX('E0000000000000000000000000000009');
SET @prod10 = UNHEX('E000000000000000000000000000000A');
SET @prod11 = UNHEX('E000000000000000000000000000000B');
SET @prod13 = UNHEX('E000000000000000000000000000000D');
SET @prod14 = UNHEX('E000000000000000000000000000000E');
SET @prod16 = UNHEX('E0000000000000000000000000000010');
SET @prod17 = UNHEX('E0000000000000000000000000000011');

-- Warehouses (from 04_warehouses.sql)
SET @wh1 = UNHEX('B0000000000000000000000000000001');
SET @wh2 = UNHEX('B0000000000000000000000000000002');
SET @wh3 = UNHEX('B0000000000000000000000000000003');
SET @wh4 = UNHEX('B0000000000000000000000000000004');

-- Users with PURCHASES module (from 01_users.sql)
SET @superadmin = UNHEX('A0EEBC999C0B4EF8BB6D6BB9BD380215');
SET @admin      = UNHEX('B1FFCD000D1C4FA9BC7E7CCACE31B326');
SET @operator   = UNHEX('C2AADE111E2D4BA0DD8F8DDBDF42C437');

-- Purchase UUIDs
SET @p1 = UNHEX('0C100000000000000000000000000001');
SET @p2 = UNHEX('0C100000000000000000000000000002');
SET @p3 = UNHEX('0C100000000000000000000000000003');
SET @p4 = UNHEX('0C100000000000000000000000000004');
SET @p5 = UNHEX('0C100000000000000000000000000005');
SET @p6 = UNHEX('0C100000000000000000000000000006');

-- Purchase Line UUIDs
SET @pl1  = UNHEX('0C200000000000000000000000000001');
SET @pl2  = UNHEX('0C200000000000000000000000000002');
SET @pl3  = UNHEX('0C200000000000000000000000000003');
SET @pl4  = UNHEX('0C200000000000000000000000000004');
SET @pl5  = UNHEX('0C200000000000000000000000000005');
SET @pl6  = UNHEX('0C200000000000000000000000000006');
SET @pl7  = UNHEX('0C200000000000000000000000000007');
SET @pl8  = UNHEX('0C200000000000000000000000000008');
SET @pl9  = UNHEX('0C200000000000000000000000000009');
SET @pl10 = UNHEX('0C20000000000000000000000000000A');
SET @pl11 = UNHEX('0C20000000000000000000000000000B');
SET @pl12 = UNHEX('0C20000000000000000000000000000C');
SET @pl13 = UNHEX('0C20000000000000000000000000000D');
SET @pl14 = UNHEX('0C20000000000000000000000000000E');

-- Purchase Line Allocation UUIDs
SET @pa1  = UNHEX('0C300000000000000000000000000001');
SET @pa2  = UNHEX('0C300000000000000000000000000002');
SET @pa3  = UNHEX('0C300000000000000000000000000003');
SET @pa4  = UNHEX('0C300000000000000000000000000004');
SET @pa5  = UNHEX('0C300000000000000000000000000005');
SET @pa6  = UNHEX('0C300000000000000000000000000006');
SET @pa7  = UNHEX('0C300000000000000000000000000007');
SET @pa8  = UNHEX('0C300000000000000000000000000008');
SET @pa9  = UNHEX('0C300000000000000000000000000009');
SET @pa10 = UNHEX('0C30000000000000000000000000000A');
SET @pa11 = UNHEX('0C30000000000000000000000000000B');
SET @pa12 = UNHEX('0C30000000000000000000000000000C');
SET @pa13 = UNHEX('0C30000000000000000000000000000D');
SET @pa14 = UNHEX('0C30000000000000000000000000000E');
SET @pa15 = UNHEX('0C30000000000000000000000000000F');
SET @pa16 = UNHEX('0C300000000000000000000000000010');

-- =============================================================================
-- PURCHASE NUMBER SEQUENCE
-- =============================================================================
REPLACE INTO purchase_number_sequence (year, next_value) VALUES (2026, 9);

-- =============================================================================
-- PURCHASE 1: DRAFT - Distribuidora Polar (3 líneas, electrónicos)
-- =============================================================================
INSERT INTO purchases (id, purchase_number, supplier_id, supplier_name, supplier_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (@p1, 'COM-2026-0001', @sup1, 'Distribuidora Polar S.A.', 'J-12345678-9', @superadmin, 'Super Admin', 'DRAFT', 'Compra de materiales eléctricos para proyecto CCS', 1475.00, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY));

INSERT INTO purchase_lines (id, purchase_id, product_id, product_name, product_code, quantity, unit_cost, line_total)
VALUES
    (@pl1, @p1, @prod1, 'Cable UTP Cat6 (metro)', 'ELEC-001', 500.0000, 1.80, 900.00),
    (@pl2, @p1, @prod4, 'Multímetro digital', 'ELEC-004', 10.0000, 30.00, 300.00),
    (@pl3, @p1, @prod2, 'Toma corriente doble', 'ELEC-002', 50.0000, 5.50, 275.00);

INSERT INTO purchase_line_allocations (id, purchase_line_id, warehouse_id, quantity)
VALUES
    (@pa1, @pl1, @wh1, 500.0000),
    (@pa2, @pl2, @wh1, 10.0000),
    (@pa3, @pl3, @wh1, 50.0000);

-- =============================================================================
-- PURCHASE 2: DRAFT - Empresas Polar (1 línea, eléctrico)
-- =============================================================================
INSERT INTO purchases (id, purchase_number, supplier_id, supplier_name, supplier_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (@p2, 'COM-2026-0002', @sup2, 'Empresas Polar C.A.', 'J-87654321-0', @admin, 'Admin User', 'DRAFT', NULL, 1000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY));

INSERT INTO purchase_lines (id, purchase_id, product_id, product_name, product_code, quantity, unit_cost, line_total)
VALUES (@pl4, @p2, @prod3, 'Breaker 20A', 'ELEC-003', 100.0000, 10.00, 1000.00);

INSERT INTO purchase_line_allocations (id, purchase_line_id, warehouse_id, quantity)
VALUES (@pa4, @pl4, @wh2, 100.0000);

-- =============================================================================
-- PURCHASE 3: CONFIRMED - Carlos Mendoza (2 líneas, lubricantes)
-- =============================================================================
INSERT INTO purchases (id, purchase_number, supplier_id, supplier_name, supplier_rif, created_by, created_by_name, status, notes, subtotal, confirmed_at, created_at, updated_at)
VALUES (@p3, 'COM-2026-0003', @sup3, 'Carlos Mendoza', 'V-11222333-4', @operator, 'Operator User', 'CONFIRMED', 'Reposición de lubricantes para taller', 1084.00, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY));

INSERT INTO purchase_lines (id, purchase_id, product_id, product_name, product_code, quantity, unit_cost, line_total)
VALUES
    (@pl5, @p3, @prod11, 'Aceite motor 20W50 (litro)', 'LUBR-001', 200.0000, 3.50, 700.00),
    (@pl6, @p3, @prod16, 'Grasa multiuso (kg)', 'LUBR-002', 80.0000, 4.80, 384.00);

INSERT INTO purchase_line_allocations (id, purchase_line_id, warehouse_id, quantity)
VALUES
    (@pa5, @pl5, @wh1, 120.0000),
    (@pa6, @pl5, @wh3, 80.0000),
    (@pa7, @pl6, @wh1, 50.0000),
    (@pa8, @pl6, @wh3, 30.0000);

-- =============================================================================
-- PURCHASE 4: CONFIRMED - Distribuidora Polar (3 líneas, herramientas)
-- =============================================================================
INSERT INTO purchases (id, purchase_number, supplier_id, supplier_name, supplier_rif, created_by, created_by_name, status, notes, subtotal, confirmed_at, created_at, updated_at)
VALUES (@p4, 'COM-2026-0004', @sup1, 'Distribuidora Polar S.A.', 'J-12345678-9', @superadmin, 'Super Admin', 'CONFIRMED', 'Compra de herramientas para nuevo almacén Valencia', 825.00, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY));

INSERT INTO purchase_lines (id, purchase_id, product_id, product_name, product_code, quantity, unit_cost, line_total)
VALUES
    (@pl7, @p4, @prod5, 'Taladro percutor 13mm', 'HERR-001', 5.0000, 85.00, 425.00),
    (@pl8, @p4, @prod6, 'Juego llaves combinadas 12pz', 'HERR-002', 10.0000, 22.00, 220.00),
    (@pl9, @p4, @prod7, 'Nivel de burbuja 60cm', 'HERR-003', 15.0000, 12.00, 180.00);

INSERT INTO purchase_line_allocations (id, purchase_line_id, warehouse_id, quantity)
VALUES
    (@pa9,  @pl7, @wh3, 5.0000),
    (@pa10, @pl8, @wh3, 10.0000),
    (@pa11, @pl9, @wh3, 15.0000);

-- =============================================================================
-- PURCHASE 5: CANCELLED - Empresas Polar (2 líneas, insumos oficina)
-- =============================================================================
INSERT INTO purchases (id, purchase_number, supplier_id, supplier_name, supplier_rif, created_by, created_by_name, status, notes, subtotal, cancelled_at, cancelled_reason, created_at, updated_at)
VALUES (@p5, 'COM-2026-0005', @sup2, 'Empresas Polar C.A.', 'J-87654321-0', @admin, 'Admin User', 'CANCELLED', NULL, 1800.00, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Proveedor no pudo cumplir con el tiempo de entrega', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO purchase_lines (id, purchase_id, product_id, product_name, product_code, quantity, unit_cost, line_total)
VALUES
    (@pl10, @p5, @prod8, 'Resma papel carta', 'OFIC-001', 300.0000, 4.80, 1440.00),
    (@pl11, @p5, @prod9, 'Tóner HP 85A', 'OFIC-002', 20.0000, 18.00, 360.00);

INSERT INTO purchase_line_allocations (id, purchase_line_id, warehouse_id, quantity)
VALUES
    (@pa12, @pl10, @wh4, 300.0000),
    (@pa13, @pl11, @wh4, 20.0000);

-- =============================================================================
-- PURCHASE 6: DRAFT - Carlos Mendoza (3 líneas, seguridad y limpieza)
-- =============================================================================
INSERT INTO purchases (id, purchase_number, supplier_id, supplier_name, supplier_rif, created_by, created_by_name, status, notes, subtotal, created_at, updated_at)
VALUES (@p6, 'COM-2026-0006', @sup3, 'Carlos Mendoza', 'V-11222333-4', @operator, 'Operator User', 'DRAFT', 'Compra de insumos de seguridad y limpieza', 790.00, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));

INSERT INTO purchase_lines (id, purchase_id, product_id, product_name, product_code, quantity, unit_cost, line_total)
VALUES
    (@pl12, @p6, @prod17, 'Desengrasante industrial (litro)', 'LIMP-001', 100.0000, 2.80, 280.00),
    (@pl13, @p6, @prod14, 'Franela industrial (kg)', 'LIMP-002', 50.0000, 3.80, 190.00),
    (@pl14, @p6, @prod13, 'Casco de seguridad', 'SEGUR-001', 40.0000, 8.00, 320.00);

INSERT INTO purchase_line_allocations (id, purchase_line_id, warehouse_id, quantity)
VALUES
    (@pa14, @pl12, @wh4, 100.0000),
    (@pa15, @pl13, @wh4, 50.0000),
    (@pa16, @pl14, @wh4, 40.0000);
