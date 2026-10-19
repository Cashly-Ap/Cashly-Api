-- =============================================================================
-- Seed: Products (simples y compuestos) + Stock en almacenes
--
-- Simples (20): base components para compuestos y venta directa
-- Compuestos (5): kits que agrupan simples
-- =============================================================================


-- -----------------------------------------------------------------------------
-- PRODUCTOS SIMPLES
-- -----------------------------------------------------------------------------
INSERT INTO products (id, code, name, description, category_id, composite, active, purchase_price)
VALUES
    -- Electrónica (cat a1)
    (UNHEX('e0000000000000000000000000000001'), 'ELEC-001', 'Cable UTP Cat6 (metro)',        'Cable de red categoría 6, venta por metro',          UNHEX('a0000000000000000000000000000001'), FALSE, TRUE, 1.80),
    (UNHEX('e0000000000000000000000000000002'), 'ELEC-002', 'Toma corriente doble',          'Toma corriente doble polarizado 15A',                UNHEX('a0000000000000000000000000000001'), FALSE, TRUE, 5.50),
    (UNHEX('e0000000000000000000000000000003'), 'ELEC-003', 'Breaker 20A',                   'Interruptor termomagnético 20 amperios',             UNHEX('a0000000000000000000000000000001'), FALSE, TRUE, 10.00),
    (UNHEX('e0000000000000000000000000000004'), 'ELEC-004', 'Multímetro digital',            'Multímetro digital con pinza amperimétrica',         UNHEX('a0000000000000000000000000000001'), FALSE, TRUE, 30.00),

    -- Herramientas (cat a2)
    (UNHEX('e0000000000000000000000000000005'), 'HERR-001', 'Taladro percutor 13mm',         'Taladro percutor 750W con maletín',                  UNHEX('a0000000000000000000000000000002'), FALSE, TRUE, 85.00),
    (UNHEX('e0000000000000000000000000000006'), 'HERR-002', 'Juego llaves combinadas 12pz',  'Set de llaves combinadas 8-19mm acero cromo',        UNHEX('a0000000000000000000000000000002'), FALSE, TRUE, 22.00),
    (UNHEX('e0000000000000000000000000000007'), 'HERR-003', 'Nivel de burbuja 60cm',         'Nivel aluminio 3 burbujas 60cm',                     UNHEX('a0000000000000000000000000000002'), FALSE, TRUE, 12.00),

    -- Insumos de Oficina (cat a3)
    (UNHEX('e0000000000000000000000000000008'), 'OFIC-001', 'Resma papel carta',             'Resma papel bond carta 75g 500 hojas',               UNHEX('a0000000000000000000000000000003'), FALSE, TRUE, 4.80),
    (UNHEX('e0000000000000000000000000000009'), 'OFIC-002', 'Tóner HP 85A',                 'Tóner negro HP CE285A compatible LaserJet',          UNHEX('a0000000000000000000000000000003'), FALSE, TRUE, 18.00),
    (UNHEX('e000000000000000000000000000000a'), 'OFIC-003', 'Bolígrafos caja x12',           'Caja 12 bolígrafos azul punta media',                UNHEX('a0000000000000000000000000000003'), FALSE, TRUE, 2.50),

    -- Lubricantes (cat a4)
    (UNHEX('e000000000000000000000000000000b'), 'LUBR-001', 'Aceite motor 20W50 (litro)',    'Aceite mineral para motor 20W50, venta por litro',   UNHEX('a0000000000000000000000000000004'), FALSE, TRUE, 3.50),
    (UNHEX('e000000000000000000000000000000c'), 'LUBR-002', 'Grasa multiuso (kg)',           'Grasa multipropósito para rodamientos, por kg',      UNHEX('a0000000000000000000000000000004'), FALSE, TRUE, 4.80),

    -- Seguridad Industrial (cat a5)
    (UNHEX('e000000000000000000000000000000d'), 'SEGUR-001', 'Casco de seguridad',           'Casco de seguridad industrial tipo II clase E',      UNHEX('a0000000000000000000000000000005'), FALSE, TRUE, 8.00),
    (UNHEX('e000000000000000000000000000000e'), 'SEGUR-002', 'Guantes de nitrilo (par)',     'Guantes de nitrilo resistentes a químicos talla M',  UNHEX('a0000000000000000000000000000005'), FALSE, TRUE, 2.00),
    (UNHEX('e000000000000000000000000000000f'), 'SEGUR-003', 'Lentes de seguridad',          'Lentes panorámicos antiempañantes clara',            UNHEX('a0000000000000000000000000000005'), FALSE, TRUE, 3.20),

    -- Limpieza (cat a6)
    (UNHEX('e0000000000000000000000000000010'), 'LIMP-001', 'Desengrasante industrial (litro)', 'Desengrasante concentrado, diluir 1:10',          UNHEX('a0000000000000000000000000000006'), FALSE, TRUE, 2.80),
    (UNHEX('e0000000000000000000000000000011'), 'LIMP-002', 'Franela industrial (kg)',       'Franela blanca para limpieza industrial por kg',     UNHEX('a0000000000000000000000000000006'), FALSE, TRUE, 3.80),

    -- Plomería (cat a9)
    (UNHEX('e0000000000000000000000000000012'), 'PLOM-001', 'Tubo PVC 1/2" (metro)',        'Tubería PVC cédula 40, venta por metro',             UNHEX('a0000000000000000000000000000009'), FALSE, TRUE, 1.10),
    (UNHEX('e0000000000000000000000000000013'), 'PLOM-002', 'Codo PVC 1/2" x 90°',         'Codo PVC presión 1/2 pulgada 90 grados',             UNHEX('a0000000000000000000000000000009'), FALSE, TRUE, 0.35),
    (UNHEX('e0000000000000000000000000000014'), 'PLOM-003', 'Llave de paso 1/2"',           'Válvula de bola PVC 1/2 pulgada',                   UNHEX('a0000000000000000000000000000009'), FALSE, TRUE, 2.20);

-- -----------------------------------------------------------------------------
-- PRODUCTOS COMPUESTOS
-- -----------------------------------------------------------------------------
INSERT INTO products (id, code, name, description, category_id, composite, active, purchase_price)
VALUES
    (UNHEX('e0000000000000000000000000000015'), 'KIT-ELEC-01',  'Kit Instalación Eléctrica Básica',  'Kit completo para instalación eléctrica residencial básica', UNHEX('a0000000000000000000000000000001'), TRUE, TRUE, 60.00),
    (UNHEX('e0000000000000000000000000000016'), 'KIT-SEGUR-01', 'Kit EPP Completo',                   'Equipo de protección personal completo para obra',           UNHEX('a0000000000000000000000000000005'), TRUE, TRUE, 16.00),
    (UNHEX('e0000000000000000000000000000017'), 'KIT-OFIC-01',  'Kit Oficina Mensual',               'Insumos de oficina para un mes de operación',               UNHEX('a0000000000000000000000000000003'), TRUE, TRUE, 48.00),
    (UNHEX('e0000000000000000000000000000018'), 'KIT-PLOM-01',  'Kit Plomería Básica 5m',            'Kit de plomería para instalación de 5 metros',              UNHEX('a0000000000000000000000000000009'), TRUE, TRUE, 12.00),
    (UNHEX('e0000000000000000000000000000019'), 'KIT-HERR-01',  'Kit Herramientas Eléctricas',       'Kit taladro con nivel y juego de llaves',                   UNHEX('a0000000000000000000000000000002'), TRUE, TRUE, 130.00);

-- -----------------------------------------------------------------------------
-- COMPONENTES DE PRODUCTOS COMPUESTOS
-- -----------------------------------------------------------------------------
INSERT INTO product_components (id, parent_product_id, component_product_id, quantity)
VALUES
    -- Kit Instalación Eléctrica: 10m cable + 4 tomas + 2 breakers
    (UNHEX('f0000000000000000000000000000001'), UNHEX('e0000000000000000000000000000015'), UNHEX('e0000000000000000000000000000001'), 10),
    (UNHEX('f0000000000000000000000000000002'), UNHEX('e0000000000000000000000000000015'), UNHEX('e0000000000000000000000000000002'), 4),
    (UNHEX('f0000000000000000000000000000003'), UNHEX('e0000000000000000000000000000015'), UNHEX('e0000000000000000000000000000003'), 2),

    -- Kit EPP: 1 casco + 2 pares guantes + 1 lentes
    (UNHEX('f0000000000000000000000000000004'), UNHEX('e0000000000000000000000000000016'), UNHEX('e000000000000000000000000000000d'), 1),
    (UNHEX('f0000000000000000000000000000005'), UNHEX('e0000000000000000000000000000016'), UNHEX('e000000000000000000000000000000e'), 2),
    (UNHEX('f0000000000000000000000000000006'), UNHEX('e0000000000000000000000000000016'), UNHEX('e000000000000000000000000000000f'), 1),

    -- Kit Oficina Mensual: 5 resmas + 1 tóner + 2 cajas bolígrafos
    (UNHEX('f0000000000000000000000000000007'), UNHEX('e0000000000000000000000000000017'), UNHEX('e0000000000000000000000000000008'), 5),
    (UNHEX('f0000000000000000000000000000008'), UNHEX('e0000000000000000000000000000017'), UNHEX('e0000000000000000000000000000009'), 1),
    (UNHEX('f0000000000000000000000000000009'), UNHEX('e0000000000000000000000000000017'), UNHEX('e000000000000000000000000000000a'), 2),

    -- Kit Plomería 5m: 5m tubo + 4 codos + 2 llaves de paso
    (UNHEX('f000000000000000000000000000000a'), UNHEX('e0000000000000000000000000000018'), UNHEX('e0000000000000000000000000000012'), 5),
    (UNHEX('f000000000000000000000000000000b'), UNHEX('e0000000000000000000000000000018'), UNHEX('e0000000000000000000000000000013'), 4),
    (UNHEX('f000000000000000000000000000000c'), UNHEX('e0000000000000000000000000000018'), UNHEX('e0000000000000000000000000000014'), 2),

    -- Kit Herramientas: 1 taladro + 1 juego llaves + 1 nivel
    (UNHEX('f000000000000000000000000000000d'), UNHEX('e0000000000000000000000000000019'), UNHEX('e0000000000000000000000000000005'), 1),
    (UNHEX('f000000000000000000000000000000e'), UNHEX('e0000000000000000000000000000019'), UNHEX('e0000000000000000000000000000006'), 1),
    (UNHEX('f000000000000000000000000000000f'), UNHEX('e0000000000000000000000000000019'), UNHEX('e0000000000000000000000000000007'), 1);

-- -----------------------------------------------------------------------------
-- STOCK EN ALMACENES
-- Warehouse stock and complete movement ledger are loaded by 13/16.
