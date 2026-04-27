-- =============================================================================
-- Seed: Warehouses (5 almacenes) + Customer Warehouse Access
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM customer_warehouse_access;
DELETE FROM stock_movements;
DELETE FROM warehouse_stock;
DELETE FROM warehouses;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO warehouses (id, code, name, address, state, municipality, postal_code, active)
VALUES
    (UNHEX('b0000000000000000000000000000001'), 'ALM-CCS-01', 'Almacén Principal Caracas',   'Zona Industrial La Yaguara, Galpón 3, Calle B', 'Distrito Capital', 'Libertador',   '1020', TRUE),
    (UNHEX('b0000000000000000000000000000002'), 'ALM-MRB-01', 'Almacén Maracaibo Norte',     'Av. La Limpia, Sector Industrial, Galpón 7',    'Zulia',            'Maracaibo',    '4001', TRUE),
    (UNHEX('b0000000000000000000000000000003'), 'ALM-VLC-01', 'Almacén Valencia',            'Zona Industrial Sur, Calle 4, Galpón 12',       'Carabobo',         'Valencia',     '2001', TRUE),
    (UNHEX('b0000000000000000000000000000004'), 'ALM-BQT-01', 'Almacén Barquisimeto',        'Av. Intercomunal, Parque Industrial, Local 5',  'Lara',             'Iribarren',    '3001', TRUE),
    (UNHEX('b0000000000000000000000000000005'), 'ALM-MCY-01', 'Almacén Maracay (Inactivo)',  'Urb. Industrial Las Tejerías, Galpón 2',        'Aragua',           'Girardot',     '2101', FALSE);

-- =============================================================================
-- Customer Warehouse Access
-- DistCaracas (c1) -> ALM-CCS-01, ALM-VLC-01
-- ImpSur (c2)      -> ALM-MRB-01
-- SumZulia (c5)    -> ALM-MRB-01, ALM-CCS-01
-- TecSol (c7)      -> ALM-CCS-01, ALM-VLC-01, ALM-BQT-01
-- AgroLlanos (c8)  -> ALM-VLC-01
-- =============================================================================
INSERT INTO customer_warehouse_access (id, customer_id, warehouse_id, granted_at, granted_by)
VALUES
    (UNHEX('d0000000000000000000000000000001'), UNHEX('c1000000000000000000000000000001'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000002'), UNHEX('c1000000000000000000000000000001'), UNHEX('b0000000000000000000000000000003'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000003'), UNHEX('c1000000000000000000000000000002'), UNHEX('b0000000000000000000000000000002'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000004'), UNHEX('c1000000000000000000000000000005'), UNHEX('b0000000000000000000000000000002'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000005'), UNHEX('c1000000000000000000000000000005'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000006'), UNHEX('c1000000000000000000000000000007'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000007'), UNHEX('c1000000000000000000000000000007'), UNHEX('b0000000000000000000000000000003'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000008'), UNHEX('c1000000000000000000000000000007'), UNHEX('b0000000000000000000000000000004'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000009'), UNHEX('c1000000000000000000000000000008'), UNHEX('b0000000000000000000000000000003'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'));
