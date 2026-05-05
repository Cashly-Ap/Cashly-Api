-- =============================================================
-- Parsun — Development Seed Data
-- Run manually: docker exec -i parsun-api-mysql mysql -u root -pverysecret mydatabase < db/seed/seed.sql
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE order_lines;
TRUNCATE TABLE orders;
TRUNCATE TABLE order_number_sequence;
TRUNCATE TABLE product_prices;
TRUNCATE TABLE product_components;
TRUNCATE TABLE product_code_sequence;
TRUNCATE TABLE products;
TRUNCATE TABLE categories;
TRUNCATE TABLE warehouse_stock;
TRUNCATE TABLE customer_warehouse_access;
TRUNCATE TABLE customer_addresses;
TRUNCATE TABLE customer_phones;
TRUNCATE TABLE customer_emails;
TRUNCATE TABLE customers;
TRUNCATE TABLE price_types;
TRUNCATE TABLE warehouses;
TRUNCATE TABLE user_module_permissions;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================
-- USERS
-- gabriel@gmail.com  / 49510561Gabriel.
-- admin@parsun.com   / password
-- vendedor@parsun.com / password
-- laura@parsun.com   / password
-- =============================================================

INSERT INTO users (id, email, user_name, password_hash, role, active, email_verified, commission_deduction_percentage, created_at, updated_at) VALUES
(UNHEX('aaaaaaaa000000000000000000000001'), 'gabriel@gmail.com',   'Gabriel',        '$2b$10$Khe6YzF3Gch.3x8esQTGD.1qtuxONgHqTcDowYIWzJrFkPAbWMewu', 'SUPER_ADMIN', TRUE, TRUE, 0.00, NOW(), NOW()),
(UNHEX('aaaaaaaa000000000000000000000002'), 'vendedor@parsun.com', 'Carlos Mendoza', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'OPERATOR', TRUE, TRUE, 1.50, NOW(), NOW()),
(UNHEX('aaaaaaaa000000000000000000000003'), 'laura@parsun.com',    'Laura Jimenez',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'OPERATOR', TRUE, TRUE, 2.00, NOW(), NOW());

INSERT INTO user_module_permissions (id, user_id, module) VALUES
(UNHEX('bbbbbbbb000000000000000000000001'), UNHEX('aaaaaaaa000000000000000000000001'), 'USERS'),
(UNHEX('bbbbbbbb000000000000000000000002'), UNHEX('aaaaaaaa000000000000000000000001'), 'SALES'),
(UNHEX('bbbbbbbb000000000000000000000003'), UNHEX('aaaaaaaa000000000000000000000001'), 'INVENTORY'),
(UNHEX('bbbbbbbb000000000000000000000004'), UNHEX('aaaaaaaa000000000000000000000001'), 'FINANCE'),
(UNHEX('bbbbbbbb000000000000000000000005'), UNHEX('aaaaaaaa000000000000000000000002'), 'SALES'),
(UNHEX('bbbbbbbb000000000000000000000006'), UNHEX('aaaaaaaa000000000000000000000002'), 'INVENTORY'),
(UNHEX('bbbbbbbb000000000000000000000007'), UNHEX('aaaaaaaa000000000000000000000003'), 'SALES'),
(UNHEX('bbbbbbbb000000000000000000000008'), UNHEX('aaaaaaaa000000000000000000000003'), 'INVENTORY');

-- =============================================================
-- WAREHOUSES
-- =============================================================

INSERT INTO warehouses (id, code, name, address, state, municipality, postal_code, active) VALUES
(UNHEX('55555555aaaa00000000000000000001'), 'ALM-001', 'Almacen Principal Caracas',  'Av. Libertador, Galpón 12, Caracas',       'Distrito Capital', 'Libertador', '1010', TRUE),
(UNHEX('55555555aaaa00000000000000000002'), 'ALM-002', 'Almacen Valencia',           'Zona Industrial Norte, Galpón 5, Valencia', 'Carabobo',         'Valencia',   '2001', TRUE),
(UNHEX('55555555aaaa00000000000000000003'), 'ALM-003', 'Almacen Maracaibo',          'Av. 5 de Julio, Local 8, Maracaibo',        'Zulia',            'Maracaibo',  '4001', FALSE);

-- =============================================================
-- PRICE TYPES
-- =============================================================

INSERT INTO price_types (id, name, description, active, created_at, updated_at) VALUES
(UNHEX('66666666bbbb00000000000000000001'), 'Precio Publico',     'Precio de venta al publico general',          TRUE,  NOW(), NOW()),
(UNHEX('66666666bbbb00000000000000000002'), 'Precio Distribuidor','Precio especial para distribuidores',         TRUE,  NOW(), NOW()),
(UNHEX('66666666bbbb00000000000000000003'), 'Precio Mayorista',   'Precio para compras al mayor (min 100 uds)',  TRUE,  NOW(), NOW()),
(UNHEX('66666666bbbb00000000000000000004'), 'Precio Exportacion', 'Precio para clientes en el exterior',         FALSE, NOW(), NOW());

-- =============================================================
-- CATEGORIES
-- =============================================================

INSERT INTO categories (id, name, description, active, created_at, updated_at) VALUES
(UNHEX('cccccccc000000000000000000000001'), 'Lubricantes', 'Aceites y lubricantes industriales',    TRUE, NOW(), NOW()),
(UNHEX('cccccccc000000000000000000000002'), 'Filtros',     'Filtros de aire, aceite y combustible', TRUE, NOW(), NOW()),
(UNHEX('cccccccc000000000000000000000003'), 'Repuestos',   'Repuestos y piezas mecanicas',          TRUE, NOW(), NOW());

-- =============================================================
-- PRODUCTS
-- =============================================================

INSERT INTO product_code_sequence VALUES (), (), (), (), ();

INSERT INTO products (id, code, name, description, category_id, composite, base_price, purchase_price, profit_margin, active, created_at, updated_at) VALUES
(UNHEX('dddddddd000000000000000000000001'), 'PRD-00001', 'Aceite Motor 20W-50 1L',       'Aceite mineral para motores de gasolina',        UNHEX('cccccccc000000000000000000000001'), FALSE,  8.50,  5.00, 70.00, TRUE, NOW(), NOW()),
(UNHEX('dddddddd000000000000000000000002'), 'PRD-00002', 'Aceite Motor 15W-40 4L',       'Aceite semisinetico para motores diesel',         UNHEX('cccccccc000000000000000000000001'), FALSE, 28.00, 17.00, 64.70, TRUE, NOW(), NOW()),
(UNHEX('dddddddd000000000000000000000003'), 'PRD-00003', 'Filtro de Aceite Universal',    'Filtro de aceite compatible multiples marcas',   UNHEX('cccccccc000000000000000000000002'), FALSE,  4.50,  2.50, 80.00, TRUE, NOW(), NOW()),
(UNHEX('dddddddd000000000000000000000004'), 'PRD-00004', 'Filtro de Aire Toyota Corolla', 'Filtro de aire original para Corolla 2015-2022', UNHEX('cccccccc000000000000000000000002'), FALSE,  6.00,  3.50, 71.43, TRUE, NOW(), NOW()),
(UNHEX('dddddddd000000000000000000000005'), 'PRD-00005', 'Pastillas de Freno Delanteras', 'Pastillas ceramicas para vehiculos livianos',    UNHEX('cccccccc000000000000000000000003'), FALSE, 22.00, 13.00, 69.23, TRUE, NOW(), NOW());

-- =============================================================
-- CUSTOMERS
-- =============================================================

INSERT INTO customers (id, code, legal_name, type, rif, alias, credit_days, credit_limit, active, portal_access, created_at, updated_at) VALUES
(UNHEX('eeeeeeee000000000000000000000001'), 1, 'Distribuidora El Exito C.A.',      'JURIDICA', 'J-12345678-9', 'El Exito',  30, 5000.00, TRUE, FALSE, NOW(), NOW()),
(UNHEX('eeeeeeee000000000000000000000002'), 2, 'Taller Mecanico Los Andes S.R.L.', 'JURIDICA', 'J-98765432-1', 'Los Andes', 15, 2000.00, TRUE, FALSE, NOW(), NOW()),
(UNHEX('eeeeeeee000000000000000000000003'), 3, 'Pedro Ramirez',                    'NATURAL',  'V-10234567-8', 'Pedro R.',   0,    0.00, TRUE, FALSE, NOW(), NOW());

INSERT INTO customer_emails (id, customer_id, email) VALUES
(UNHEX('ffffffff000000000000000000000001'), UNHEX('eeeeeeee000000000000000000000001'), 'compras@elexito.com'),
(UNHEX('ffffffff000000000000000000000002'), UNHEX('eeeeeeee000000000000000000000002'), 'taller@losandes.com'),
(UNHEX('ffffffff000000000000000000000003'), UNHEX('eeeeeeee000000000000000000000003'), 'pedro.ramirez@gmail.com');

INSERT INTO customer_phones (id, customer_id, phone) VALUES
(UNHEX('11111111aaaa00000000000000000001'), UNHEX('eeeeeeee000000000000000000000001'), '0212-5551234'),
(UNHEX('11111111aaaa00000000000000000002'), UNHEX('eeeeeeee000000000000000000000002'), '0274-5559876'),
(UNHEX('11111111aaaa00000000000000000003'), UNHEX('eeeeeeee000000000000000000000003'), '0414-1234567');

INSERT INTO customer_addresses (id, customer_id, address, billing, active, state, municipality, postal_code) VALUES
(UNHEX('22222222bbbb00000000000000000001'), UNHEX('eeeeeeee000000000000000000000001'), 'Av. Principal, Local 5, Caracas',       TRUE, TRUE, 'Distrito Capital', 'Libertador', '1010'),
(UNHEX('22222222bbbb00000000000000000002'), UNHEX('eeeeeeee000000000000000000000002'), 'Calle 5, Galpon 3, Merida',             TRUE, TRUE, 'Merida',           'Libertador', '5101'),
(UNHEX('22222222bbbb00000000000000000003'), UNHEX('eeeeeeee000000000000000000000003'), 'Urb. Las Flores, Casa 12, Valencia',    TRUE, TRUE, 'Carabobo',         'Valencia',   '2001');

-- =============================================================
-- ORDERS
-- =============================================================

INSERT INTO order_number_sequence (year, next_value) VALUES (2026, 4);

INSERT INTO orders (id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, delivery_date, notes, subtotal, created_at, updated_at) VALUES
(
    UNHEX('33333333cccc00000000000000000001'),
    'PED-2026-00001',
    UNHEX('eeeeeeee000000000000000000000001'),
    'Distribuidora El Exito C.A.',
    'J-12345678-9',
    UNHEX('aaaaaaaa000000000000000000000002'),
    'Carlos Mendoza',
    'INVOICED',
    '2026-05-15',
    'Entrega en almacen principal',
    135.50,
    NOW(), NOW()
),
(
    UNHEX('33333333cccc00000000000000000002'),
    'PED-2026-00002',
    UNHEX('eeeeeeee000000000000000000000002'),
    'Taller Mecanico Los Andes S.R.L.',
    'J-98765432-1',
    UNHEX('aaaaaaaa000000000000000000000003'),
    'Laura Jimenez',
    'DRAFT',
    '2026-05-20',
    NULL,
    56.00,
    NOW(), NOW()
),
(
    UNHEX('33333333cccc00000000000000000003'),
    'PED-2026-00003',
    UNHEX('eeeeeeee000000000000000000000003'),
    'Pedro Ramirez',
    'V-10234567-8',
    UNHEX('aaaaaaaa000000000000000000000002'),
    'Carlos Mendoza',
    'CANCELLED',
    '2026-05-10',
    NULL,
    22.00,
    NOW(), NOW()
);

UPDATE orders
SET cancelled_at = NOW(), cancelled_reason = 'Cliente solicito cancelacion'
WHERE id = UNHEX('33333333cccc00000000000000000003');

-- =============================================================
-- ORDER LINES
-- =============================================================

-- PED-2026-00001: Aceite 20W-50 x10 + Aceite 15W-40 x1 + Filtro Aceite x5
INSERT INTO order_lines (id, order_id, product_id, product_name, product_code, quantity, unit_price, line_total) VALUES
(UNHEX('44444444dddd00000000000000000001'), UNHEX('33333333cccc00000000000000000001'), UNHEX('dddddddd000000000000000000000001'), 'Aceite Motor 20W-50 1L',       'PRD-00001', 10.0000,  8.50,  85.00),
(UNHEX('44444444dddd00000000000000000002'), UNHEX('33333333cccc00000000000000000001'), UNHEX('dddddddd000000000000000000000002'), 'Aceite Motor 15W-40 4L',       'PRD-00002',  1.0000, 28.00,  28.00),
(UNHEX('44444444dddd00000000000000000003'), UNHEX('33333333cccc00000000000000000001'), UNHEX('dddddddd000000000000000000000003'), 'Filtro de Aceite Universal',   'PRD-00003',  5.0000,  4.50,  22.50);

-- PED-2026-00002: Filtro Aire x2 + Pastillas Freno x2
INSERT INTO order_lines (id, order_id, product_id, product_name, product_code, quantity, unit_price, line_total) VALUES
(UNHEX('44444444dddd00000000000000000004'), UNHEX('33333333cccc00000000000000000002'), UNHEX('dddddddd000000000000000000000004'), 'Filtro de Aire Toyota Corolla', 'PRD-00004', 2.0000,  6.00,  12.00),
(UNHEX('44444444dddd00000000000000000005'), UNHEX('33333333cccc00000000000000000002'), UNHEX('dddddddd000000000000000000000005'), 'Pastillas de Freno Delanteras', 'PRD-00005', 2.0000, 22.00,  44.00);

-- PED-2026-00003 (CANCELLED): Pastillas Freno x1
INSERT INTO order_lines (id, order_id, product_id, product_name, product_code, quantity, unit_price, line_total) VALUES
(UNHEX('44444444dddd00000000000000000006'), UNHEX('33333333cccc00000000000000000003'), UNHEX('dddddddd000000000000000000000005'), 'Pastillas de Freno Delanteras', 'PRD-00005', 1.0000, 22.00,  22.00);

-- =============================================================
-- WAREHOUSE STOCK
-- Almacen Principal (ALM-001): stock completo
-- Almacen Valencia  (ALM-002): stock parcial
-- =============================================================

INSERT INTO warehouse_stock (id, warehouse_id, product_id, quantity) VALUES
(UNHEX('77777777cccc00000000000000000001'), UNHEX('55555555aaaa00000000000000000001'), UNHEX('dddddddd000000000000000000000001'), 150),
(UNHEX('77777777cccc00000000000000000002'), UNHEX('55555555aaaa00000000000000000001'), UNHEX('dddddddd000000000000000000000002'),  80),
(UNHEX('77777777cccc00000000000000000003'), UNHEX('55555555aaaa00000000000000000001'), UNHEX('dddddddd000000000000000000000003'), 200),
(UNHEX('77777777cccc00000000000000000004'), UNHEX('55555555aaaa00000000000000000001'), UNHEX('dddddddd000000000000000000000004'),  60),
(UNHEX('77777777cccc00000000000000000005'), UNHEX('55555555aaaa00000000000000000001'), UNHEX('dddddddd000000000000000000000005'),  45),
(UNHEX('77777777cccc00000000000000000006'), UNHEX('55555555aaaa00000000000000000002'), UNHEX('dddddddd000000000000000000000001'),  50),
(UNHEX('77777777cccc00000000000000000007'), UNHEX('55555555aaaa00000000000000000002'), UNHEX('dddddddd000000000000000000000003'),  75),
(UNHEX('77777777cccc00000000000000000008'), UNHEX('55555555aaaa00000000000000000002'), UNHEX('dddddddd000000000000000000000005'),  20);
