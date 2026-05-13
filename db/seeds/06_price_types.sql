-- =============================================================================
-- Seed: Price Types (3 tipos de precio)
-- Referenciados por seed-customer-warehouses-and-prices.sql
-- =============================================================================

DELETE FROM product_prices;
DELETE FROM price_types;

INSERT INTO price_types (id, name, description, active, created_at, updated_at)
VALUES
    (UNHEX('fa000000000000000000000000000001'), 'Precio Base',    'Precio base de venta al público',    TRUE, NOW(), NOW()),
    (UNHEX('fa000000000000000000000000000002'), 'Precio Mayor',   'Precio por volumen/mayoreo',         TRUE, NOW(), NOW()),
    (UNHEX('fa000000000000000000000000000003'), 'Precio Especial','Precio especial para clientes VIP', TRUE, NOW(), NOW());
