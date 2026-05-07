-- =============================================================================
-- Seed: Customer Warehouse Access + Price Type Assignment
-- Customers : c1000000-0000-0000-0000-00000000000{1..10}
-- Warehouses: b0000000-0000-0000-0000-00000000000{1..5}
--   0001 = ALM-CCS-01  Almacen Principal Caracas
--   0002 = ALM-MRB-01  Almacen Maracaibo Norte
--   0003 = ALM-VLC-01  Almacen Valencia
--   0004 = ALM-BQT-01  Almacen Barquisimeto
--   0005 = ALM-MCY-01  Almacen Maracay (Inactivo)
-- Price types: fa000000-0000-0000-0000-00000000000{1..3}
--   0001 = Precio Base
--   0002 = Precio Mayor
--   0003 = Precio Especial
-- =============================================================================

-- granted_by = admin user (first SUPER_ADMIN seed)
SET @admin_id = UUID_TO_BIN('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380215');

-- -----------------------------------------------------------------------------
-- 1001 | Distribuidora Caracas C.A. | BUSINESS
--      Precio Especial | Caracas + Valencia + Barquisimeto
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000003')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000001');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000001'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000003'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000001'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000004'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1002 | Importaciones del Sur C.A. | BUSINESS
--      Precio Mayor | Caracas + Valencia
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000002')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000002');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000002'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000001'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000002'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000003'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1003 | Comercializadora Norte S.R.L. | BUSINESS
--      Precio Mayor | Maracaibo + Barquisimeto
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000002')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000003');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000003'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000002'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000003'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000004'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1004 | Carlos Eduardo Mendoza | INDIVIDUAL
--      Precio Base | Caracas
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000001')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000004');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000004'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000001'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1005 | Suministros Industriales Zulia C.A. | BUSINESS
--      Precio Especial | Maracaibo + Barquisimeto + Maracay
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000003')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000005');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000005'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000002'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000005'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000004'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000005'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000005'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1006 | Maria Alejandra Torres | INDIVIDUAL
--      Precio Base | Valencia
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000001')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000006');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000006'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000003'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1007 | Tecnologia y Soluciones C.A. | BUSINESS
--      Precio Especial | Caracas + Maracaibo + Valencia
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000003')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000007');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000007'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000001'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000007'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000002'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000007'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000003'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1008 | Agropecuaria Los Llanos S.R.L. | BUSINESS
--      Precio Mayor | Barquisimeto + Maracay
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000002')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000008');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000008'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000004'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000008'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000005'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1009 | Roberto Jose Perez | INDIVIDUAL
--      Precio Base | Caracas + Maracay
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000001')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000009');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000009'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000001'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000009'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000005'), @admin_id);

-- -----------------------------------------------------------------------------
-- 1010 | Constructora Andina C.A. | BUSINESS
--      Precio Mayor | Caracas + Valencia + Barquisimeto + Maracay
-- -----------------------------------------------------------------------------
UPDATE customers
SET price_type_id = UUID_TO_BIN('fa000000-0000-0000-0000-000000000002')
WHERE id = UUID_TO_BIN('c1000000-0000-0000-0000-000000000010');

INSERT IGNORE INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_by)
VALUES
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000010'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000001'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000010'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000003'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000010'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000004'), @admin_id),
    (UUID_TO_BIN(UUID()), UUID_TO_BIN('c1000000-0000-0000-0000-000000000010'), UUID_TO_BIN('b0000000-0000-0000-0000-000000000005'), @admin_id);
