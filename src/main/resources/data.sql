-- ============================================================
-- SEED DATA: Suppliers
-- Ejecutar manualmente después de que las migraciones Flyway
-- hayan creado las tablas. O configurar:
--   spring.sql.init.mode=always
-- en application-dev.properties
-- ============================================================

-- Convertir UUID strings a BINARY(16) usando UUID_TO_BIN()
-- Los IDs se generan fijos para permitir referencias entre tablas

SET @sup1 = UUID_TO_BIN('a1000000-0000-0000-0000-000000000001');
SET @sup2 = UUID_TO_BIN('a2000000-0000-0000-0000-000000000002');
SET @sup3 = UUID_TO_BIN('a3000000-0000-0000-0000-000000000003');

INSERT INTO suppliers (id, code, legal_name, alias, type, rif, notes, active, created_at, updated_at)
VALUES
    (@sup1, 1001, 'Distribuidora Polar S.A.', 'Polar', 'JURIDICO', 'J-12345678-9', 'Principal proveedor de bebidas', TRUE, NOW(), NOW()),
    (@sup2, 1002, 'Empresas Polar C.A.', 'EPC', 'JURIDICO', 'J-87654321-0', NULL, TRUE, NOW(), NOW()),
    (@sup3, 1003, 'Carlos Mendoza', 'Mendoza', 'NATURAL', 'V-11222333-4', 'Proveedor independiente de vegetales', TRUE, NOW(), NOW());

-- Emails
INSERT INTO supplier_emails (id, supplier_id, email)
VALUES
    (UUID_TO_BIN(UUID()), @sup1, 'compras@polar.com.ve'),
    (UUID_TO_BIN(UUID()), @sup1, 'ventas@polar.com.ve'),
    (UUID_TO_BIN(UUID()), @sup2, 'contacto@epc.com.ve'),
    (UUID_TO_BIN(UUID()), @sup3, 'cmendoza@gmail.com');

-- Phones
INSERT INTO supplier_phones (id, supplier_id, phone)
VALUES
    (UUID_TO_BIN(UUID()), @sup1, '+58 212 555 0101'),
    (UUID_TO_BIN(UUID()), @sup1, '+58 212 555 0102'),
    (UUID_TO_BIN(UUID()), @sup2, '+58 241 555 0200'),
    (UUID_TO_BIN(UUID()), @sup3, '+58 414 123 4567');

-- Addresses
INSERT INTO supplier_addresses (id, supplier_id, address, billing, active, state, municipality, postal_code)
VALUES
    (UUID_TO_BIN(UUID()), @sup1, 'Av. Francisco de Miranda, Torre Polar, Piso 10, Caracas', TRUE, TRUE, 'Distrito Capital', 'Libertador', '1060'),
    (UUID_TO_BIN(UUID()), @sup2, 'Zona Industrial La Urbina, Calle 5, Edif. EPC, Caracas', TRUE, TRUE, 'Miranda', 'Sucre', '1073'),
    (UUID_TO_BIN(UUID()), @sup3, 'Calle Principal, Casa #45, Sector El Valle, Caracas', FALSE, TRUE, 'Distrito Capital', 'Libertador', '1090');
