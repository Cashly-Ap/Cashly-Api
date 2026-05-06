-- =============================================================================
-- Seed: Orders + Order Lines
--
-- Statuses: PENDING, CONFIRMED, DELIVERED, CANCELLED
-- Created by: gabriel@gmail.com (f5ddea444b5a7ed3cc1b1aaed175f76a)
--
-- Orders:
--   ORD-2026-0001  DistCaracas     INVOICED
--   ORD-2026-0002  ImpSur          INVOICED
--   ORD-2026-0003  TecSol          CONFIRMED
--   ORD-2026-0004  SumZulia        DRAFT
--   ORD-2026-0005  AgroLlanos      DRAFT
--   ORD-2026-0006  ComNorte        CANCELLED
--   ORD-2026-0007  DistCaracas     CONFIRMED
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM order_lines;
DELETE FROM orders;
DELETE FROM order_number_sequence;
SET FOREIGN_KEY_CHECKS = 1;

-- Sequence: year 2026, next value starts at 8 (7 orders already used)
INSERT INTO order_number_sequence (year, next_value)
VALUES (2026, 8);

-- =============================================================================
-- ORDERS
-- =============================================================================
INSERT INTO orders (id, order_number, customer_id, customer_name, customer_rif, created_by, created_by_name, status, delivery_date, notes, subtotal, cancelled_at, cancelled_reason, created_at, updated_at)
VALUES
    -- ORD-2026-0001: DistCaracas — DELIVERED
    (
        UNHEX('aa000000000000000000000000000001'),
        'ORD-2026-0001',
        UNHEX('c1000000000000000000000000000001'),
        'Distribuidora Caracas C.A.',
        'J-30123456-7',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'INVOICED',
        '2026-04-15',
        NULL,
        285.00,
        NULL,
        NULL,
        '2026-04-10 09:00:00',
        '2026-04-15 14:30:00'
    ),

    -- ORD-2026-0002: ImpSur — DELIVERED
    (
        UNHEX('aa000000000000000000000000000002'),
        'ORD-2026-0002',
        UNHEX('c1000000000000000000000000000002'),
        'Importaciones del Sur C.A.',
        'J-20987654-3',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'INVOICED',
        '2026-04-20',
        'Entregar en horario de mañana',
        370.00,
        NULL,
        NULL,
        '2026-04-14 11:00:00',
        '2026-04-20 10:15:00'
    ),

    -- ORD-2026-0003: TecSol — CONFIRMED
    (
        UNHEX('aa000000000000000000000000000003'),
        'ORD-2026-0003',
        UNHEX('c1000000000000000000000000000007'),
        'Tecnologia y Soluciones C.A.',
        'J-50345678-9',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'CONFIRMED',
        '2026-05-10',
        NULL,
        555.00,
        NULL,
        NULL,
        '2026-04-28 08:30:00',
        '2026-04-28 09:00:00'
    ),

    -- ORD-2026-0004: SumZulia — PENDING
    (
        UNHEX('aa000000000000000000000000000004'),
        'ORD-2026-0004',
        UNHEX('c1000000000000000000000000000005'),
        'Suministros Industriales Zulia C.A.',
        'J-40234567-8',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'DRAFT',
        '2026-05-15',
        'Cliente solicita factura a nombre de la empresa',
        175.00,
        NULL,
        NULL,
        '2026-05-01 10:00:00',
        '2026-05-01 10:00:00'
    ),

    -- ORD-2026-0005: AgroLlanos — PENDING
    (
        UNHEX('aa000000000000000000000000000005'),
        'ORD-2026-0005',
        UNHEX('c1000000000000000000000000000008'),
        'Agropecuaria Los Llanos S.R.L.',
        'J-11456789-0',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'DRAFT',
        '2026-05-20',
        NULL,
        108.00,
        NULL,
        NULL,
        '2026-05-02 14:00:00',
        '2026-05-02 14:00:00'
    ),

    -- ORD-2026-0006: ComNorte — CANCELLED
    (
        UNHEX('aa000000000000000000000000000006'),
        'ORD-2026-0006',
        UNHEX('c1000000000000000000000000000003'),
        'Comercializadora Norte S.R.L.',
        'J-10555444-1',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'CANCELLED',
        '2026-05-08',
        NULL,
        70.00,
        '2026-05-03 16:00:00',
        'Cliente canceló por falta de presupuesto',
        '2026-05-02 09:00:00',
        '2026-05-03 16:00:00'
    ),

    -- ORD-2026-0007: DistCaracas — CONFIRMED
    (
        UNHEX('aa000000000000000000000000000007'),
        'ORD-2026-0007',
        UNHEX('c1000000000000000000000000000001'),
        'Distribuidora Caracas C.A.',
        'J-30123456-7',
        UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'),
        'Gabriel Admin',
        'CONFIRMED',
        '2026-05-22',
        'Segunda orden del mes',
        240.00,
        NULL,
        NULL,
        '2026-05-04 11:30:00',
        '2026-05-04 12:00:00'
    );

-- =============================================================================
-- ORDER LINES
-- =============================================================================
INSERT INTO order_lines (id, order_id, product_id, product_name, product_code, quantity, unit_price, line_total)
VALUES
    -- ORD-2026-0001: DistCaracas DELIVERED
    --   50m Cable UTP Cat6 @ 2.50 = 125.00
    --   10x Toma corriente doble @ 8.00 = 80.00
    --   4x Breaker 20A @ 15.00 = 60.00
    --   1x Kit EPP Completo @ 25.00 = 25.00  (subtotal: 290 — ajustado a 285 con descuento implícito, usamos precios exactos)
    (UNHEX('ab000000000000000000000000000001'), UNHEX('aa000000000000000000000000000001'), UNHEX('e0000000000000000000000000000001'), 'Cable UTP Cat6 (metro)',       'ELEC-001',    50.0000, 2.50,  125.00),
    (UNHEX('ab000000000000000000000000000002'), UNHEX('aa000000000000000000000000000001'), UNHEX('e0000000000000000000000000000002'), 'Toma corriente doble',         'ELEC-002',    10.0000, 8.00,   80.00),
    (UNHEX('ab000000000000000000000000000003'), UNHEX('aa000000000000000000000000000001'), UNHEX('e0000000000000000000000000000003'), 'Breaker 20A',                  'ELEC-003',     4.0000, 15.00,  60.00),
    (UNHEX('ab000000000000000000000000000004'), UNHEX('aa000000000000000000000000000001'), UNHEX('e0000000000000000000000000000016'), 'Kit EPP Completo',             'KIT-SEGUR-01', 1.0000, 25.00,  25.00),

    -- ORD-2026-0002: ImpSur DELIVERED
    --   5x Taladro percutor 13mm @ 120.00 = 600.00  -> demasiado, usamos cantidades menores
    --   2x Taladro percutor 13mm @ 120.00 = 240.00
    --   2x Juego llaves combinadas @ 35.00 = 70.00
    --   1x Multímetro digital @ 45.00 = 45.00  (subtotal: 355 — ajustamos a 370 con 1 kit herramientas)
    --   Recalculamos: 2x Taladro=240 + 2x Llaves=70 + 1x Multímetro=45 + 1x Kit Herramientas=185 = 540 (mucho)
    --   Usamos: 2x Taladro=240 + 1x Llaves=35 + 1x Multímetro=45 + 1x Nivel=18 = 338 (ajustamos subtotal a 338)
    --   Nota: subtotal en orders ya insertado como 370, recalculamos lines para que cuadre:
    --   2x Taladro=240 + 2x Llaves=70 + 1x Multímetro=45 + 1x Nivel=18 = 373 ~ usamos 370 redondeado
    --   Simplificamos: 2x Taladro=240 + 2x Llaves=70 + 1x Multímetro=45 + 1x Nivel=15 (precio ajustado no aplica)
    --   Decisión final: ajustamos subtotal de la order a lo que sumen las lines exactamente
    --   2x Taladro @ 120 = 240, 2x Llaves @ 35 = 70, 1x Multímetro @ 45 = 45, 1x Nivel @ 18 = 18 → TOTAL = 373
    --   (El subtotal en orders fue insertado como 370, pero las lines suman 373 — usamos 373 como correcto)
    --   Para evitar inconsistencia, usamos: 2x Taladro=240 + 2x Llaves=70 + 1x Multímetro=45 + 1x Nivel=18 = 373
    (UNHEX('ab000000000000000000000000000005'), UNHEX('aa000000000000000000000000000002'), UNHEX('e0000000000000000000000000000005'), 'Taladro percutor 13mm',        'HERR-001',     2.0000, 120.00, 240.00),
    (UNHEX('ab000000000000000000000000000006'), UNHEX('aa000000000000000000000000000002'), UNHEX('e0000000000000000000000000000006'), 'Juego llaves combinadas 12pz', 'HERR-002',     2.0000, 35.00,   70.00),
    (UNHEX('ab000000000000000000000000000007'), UNHEX('aa000000000000000000000000000002'), UNHEX('e0000000000000000000000000000004'), 'Multímetro digital',           'ELEC-004',     1.0000, 45.00,   45.00),
    (UNHEX('ab000000000000000000000000000008'), UNHEX('aa000000000000000000000000000002'), UNHEX('e0000000000000000000000000000007'), 'Nivel de burbuja 60cm',        'HERR-003',     1.0000, 18.00,   18.00),

    -- ORD-2026-0003: TecSol CONFIRMED
    --   2x Kit Instalación Eléctrica @ 85.00 = 170.00
    --   3x Kit EPP Completo @ 25.00 = 75.00
    --   5x Resma papel carta @ 6.50 = 32.50
    --   1x Tóner HP 85A @ 28.00 = 28.00
    --   10x Casco de seguridad @ 12.00 = 120.00
    --   20x Guantes de nitrilo @ 3.50 = 70.00
    --   1x Multímetro digital @ 45.00 = 45.00
    --   Total = 170+75+32.50+28+120+70+45 = 540.50 → ajustamos subtotal a 540.50
    (UNHEX('ab000000000000000000000000000009'), UNHEX('aa000000000000000000000000000003'), UNHEX('e0000000000000000000000000000015'), 'Kit Instalación Eléctrica Básica', 'KIT-ELEC-01',  2.0000, 85.00,  170.00),
    (UNHEX('ab00000000000000000000000000000a'), UNHEX('aa000000000000000000000000000003'), UNHEX('e0000000000000000000000000000016'), 'Kit EPP Completo',             'KIT-SEGUR-01', 3.0000, 25.00,   75.00),
    (UNHEX('ab00000000000000000000000000000b'), UNHEX('aa000000000000000000000000000003'), UNHEX('e0000000000000000000000000000008'), 'Resma papel carta',            'OFIC-001',     5.0000, 6.50,    32.50),
    (UNHEX('ab00000000000000000000000000000c'), UNHEX('aa000000000000000000000000000003'), UNHEX('e0000000000000000000000000000009'), 'Tóner HP 85A',                 'OFIC-002',     1.0000, 28.00,   28.00),
    (UNHEX('ab00000000000000000000000000000d'), UNHEX('aa000000000000000000000000000003'), UNHEX('e000000000000000000000000000000d'), 'Casco de seguridad',           'SEGUR-001',   10.0000, 12.00,  120.00),
    (UNHEX('ab00000000000000000000000000000e'), UNHEX('aa000000000000000000000000000003'), UNHEX('e000000000000000000000000000000e'), 'Guantes de nitrilo (par)',     'SEGUR-002',   20.0000, 3.50,    70.00),
    (UNHEX('ab00000000000000000000000000000f'), UNHEX('aa000000000000000000000000000003'), UNHEX('e0000000000000000000000000000004'), 'Multímetro digital',           'ELEC-004',     1.0000, 45.00,   45.00),

    -- ORD-2026-0004: SumZulia PENDING
    --   10x Aceite motor 20W50 @ 5.00 = 50.00
    --   5x Grasa multiuso @ 7.00 = 35.00
    --   20x Guantes de nitrilo @ 3.50 = 70.00
    --   Total = 155.00
    (UNHEX('ab000000000000000000000000000010'), UNHEX('aa000000000000000000000000000004'), UNHEX('e000000000000000000000000000000b'), 'Aceite motor 20W50 (litro)',   'LUBR-001',    10.0000, 5.00,    50.00),
    (UNHEX('ab000000000000000000000000000011'), UNHEX('aa000000000000000000000000000004'), UNHEX('e000000000000000000000000000000c'), 'Grasa multiuso (kg)',          'LUBR-002',     5.0000, 7.00,    35.00),
    (UNHEX('ab000000000000000000000000000012'), UNHEX('aa000000000000000000000000000004'), UNHEX('e000000000000000000000000000000e'), 'Guantes de nitrilo (par)',     'SEGUR-002',   20.0000, 3.50,    70.00),

    -- ORD-2026-0005: AgroLlanos PENDING
    --   20m Tubo PVC 1/2" @ 1.80 = 36.00
    --   10x Codo PVC 1/2" @ 0.60 = 6.00
    --   6x Llave de paso 1/2" @ 3.50 = 21.00
    --   5x Desengrasante industrial @ 4.50 = 22.50
    --   5x Franela industrial @ 6.00 = 30.00
    --   Total = 115.50
    (UNHEX('ab000000000000000000000000000013'), UNHEX('aa000000000000000000000000000005'), UNHEX('e0000000000000000000000000000012'), 'Tubo PVC 1/2" (metro)',        'PLOM-001',    20.0000, 1.80,    36.00),
    (UNHEX('ab000000000000000000000000000014'), UNHEX('aa000000000000000000000000000005'), UNHEX('e0000000000000000000000000000013'), 'Codo PVC 1/2" x 90°',         'PLOM-002',    10.0000, 0.60,     6.00),
    (UNHEX('ab000000000000000000000000000015'), UNHEX('aa000000000000000000000000000005'), UNHEX('e0000000000000000000000000000014'), 'Llave de paso 1/2"',           'PLOM-003',     6.0000, 3.50,    21.00),
    (UNHEX('ab000000000000000000000000000016'), UNHEX('aa000000000000000000000000000005'), UNHEX('e0000000000000000000000000000010'), 'Desengrasante industrial (litro)', 'LIMP-001', 5.0000, 4.50,   22.50),
    (UNHEX('ab000000000000000000000000000017'), UNHEX('aa000000000000000000000000000005'), UNHEX('e0000000000000000000000000000011'), 'Franela industrial (kg)',      'LIMP-002',     5.0000, 6.00,    30.00),

    -- ORD-2026-0006: ComNorte CANCELLED
    --   1x Kit Oficina Mensual @ 70.00 = 70.00
    (UNHEX('ab000000000000000000000000000018'), UNHEX('aa000000000000000000000000000006'), UNHEX('e0000000000000000000000000000017'), 'Kit Oficina Mensual',          'KIT-OFIC-01',  1.0000, 70.00,   70.00),

    -- ORD-2026-0007: DistCaracas CONFIRMED
    --   4x Taladro percutor 13mm @ 120.00 = 480.00  -> demasiado
    --   1x Kit Herramientas Eléctricas @ 185.00 = 185.00
    --   1x Juego llaves combinadas @ 35.00 = 35.00
    --   1x Nivel de burbuja @ 18.00 = 18.00
    --   Total = 238.00 → ajustamos subtotal a 238.00
    (UNHEX('ab000000000000000000000000000019'), UNHEX('aa000000000000000000000000000007'), UNHEX('e0000000000000000000000000000019'), 'Kit Herramientas Eléctricas',  'KIT-HERR-01',  1.0000, 185.00, 185.00),
    (UNHEX('ab00000000000000000000000000001a'), UNHEX('aa000000000000000000000000000007'), UNHEX('e0000000000000000000000000000006'), 'Juego llaves combinadas 12pz', 'HERR-002',     1.0000, 35.00,   35.00),
    (UNHEX('ab00000000000000000000000000001b'), UNHEX('aa000000000000000000000000000007'), UNHEX('e0000000000000000000000000000007'), 'Nivel de burbuja 60cm',        'HERR-003',     1.0000, 18.00,   18.00);

-- =============================================================================
-- Fix subtotals to match actual line totals
-- =============================================================================
UPDATE orders SET subtotal = 290.00 WHERE id = UNHEX('aa000000000000000000000000000001');  -- 125+80+60+25
UPDATE orders SET subtotal = 373.00 WHERE id = UNHEX('aa000000000000000000000000000002');  -- 240+70+45+18
UPDATE orders SET subtotal = 540.50 WHERE id = UNHEX('aa000000000000000000000000000003');  -- 170+75+32.50+28+120+70+45
UPDATE orders SET subtotal = 155.00 WHERE id = UNHEX('aa000000000000000000000000000004');  -- 50+35+70
UPDATE orders SET subtotal = 115.50 WHERE id = UNHEX('aa000000000000000000000000000005');  -- 36+6+21+22.50+30
UPDATE orders SET subtotal =  70.00 WHERE id = UNHEX('aa000000000000000000000000000006');  -- 70
UPDATE orders SET subtotal = 238.00 WHERE id = UNHEX('aa000000000000000000000000000007');  -- 185+35+18
