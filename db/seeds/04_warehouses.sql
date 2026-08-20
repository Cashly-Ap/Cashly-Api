-- =============================================================================
-- Seed: Warehouses (5 almacenes) + Customer Warehouse Access
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM customer_warehouse_access;
-- Note: stock_movements y warehouse_stock se crean en seed 13
-- DELETE FROM stock_movements;
-- DELETE FROM warehouse_stock;
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
-- Granted for every warehouse a customer's orders/invoices/quotes/returns
-- actually allocate stock from further down the seed chain (06-12) — a
-- customer can never appear in an allocation for a warehouse they can't
-- access in the real app, so this list must stay a superset of that usage.
-- DistCaracas (c1) -> ALM-CCS-01, ALM-VLC-01, ALM-MRB-01
-- ImpSur (c2)      -> ALM-MRB-01, ALM-CCS-01
-- ComNorte (c3)    -> ALM-VLC-01
-- CMendoza (c4)    -> ALM-CCS-01
-- SumZulia (c5)    -> ALM-MRB-01, ALM-CCS-01, ALM-VLC-01, ALM-BQT-01
-- TecSol (c7)      -> ALM-CCS-01, ALM-VLC-01, ALM-BQT-01
-- AgroLlanos (c8)  -> ALM-VLC-01, ALM-CCS-01
-- InvGlobal (c11)  -> ALM-CCS-01
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
    (UNHEX('d0000000000000000000000000000009'), UNHEX('c1000000000000000000000000000008'), UNHEX('b0000000000000000000000000000003'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d000000000000000000000000000000A'), UNHEX('c1000000000000000000000000000011'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d000000000000000000000000000000B'), UNHEX('c1000000000000000000000000000002'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d000000000000000000000000000000C'), UNHEX('c1000000000000000000000000000008'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d000000000000000000000000000000D'), UNHEX('c1000000000000000000000000000003'), UNHEX('b0000000000000000000000000000003'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d000000000000000000000000000000E'), UNHEX('c1000000000000000000000000000004'), UNHEX('b0000000000000000000000000000001'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d000000000000000000000000000000F'), UNHEX('c1000000000000000000000000000001'), UNHEX('b0000000000000000000000000000002'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000010'), UNHEX('c1000000000000000000000000000005'), UNHEX('b0000000000000000000000000000003'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a')),
    (UNHEX('d0000000000000000000000000000011'), UNHEX('c1000000000000000000000000000005'), UNHEX('b0000000000000000000000000000004'), NOW(), UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'));
