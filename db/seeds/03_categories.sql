-- =============================================================================
-- Seed: Categories (10 categorías variadas)
-- =============================================================================

DELETE FROM categories;

INSERT INTO categories (id, name, description, active)
VALUES
    (UNHEX('a0000000000000000000000000000001'), 'Electrónica',          'Equipos electrónicos, cables y accesorios',                TRUE),
    (UNHEX('a0000000000000000000000000000002'), 'Herramientas',         'Herramientas manuales y eléctricas para construcción',     TRUE),
    (UNHEX('a0000000000000000000000000000003'), 'Insumos de Oficina',   'Papelería, tóners, resmas y materiales de oficina',        TRUE),
    (UNHEX('a0000000000000000000000000000004'), 'Lubricantes',          'Aceites, grasas y lubricantes industriales',               TRUE),
    (UNHEX('a0000000000000000000000000000005'), 'Seguridad Industrial', 'EPP, cascos, guantes, lentes y arneses',                   TRUE),
    (UNHEX('a0000000000000000000000000000006'), 'Limpieza',             'Productos de limpieza industrial y doméstica',             TRUE),
    (UNHEX('a0000000000000000000000000000007'), 'Alimentos',            'Alimentos no perecederos y productos de consumo masivo',   TRUE),
    (UNHEX('a0000000000000000000000000000008'), 'Repuestos',            'Repuestos mecánicos y eléctricos industriales',            TRUE),
    (UNHEX('a0000000000000000000000000000009'), 'Plomería',             'Tuberías, conexiones, válvulas y accesorios de plomería',  TRUE),
    (UNHEX('a000000000000000000000000000000a'), 'Textil Industrial',    'Uniformes, overoles, botas y ropa de trabajo',             FALSE);
