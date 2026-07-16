-- =============================================================================
-- Seed: Warehouse Movements with Traceability + Customer Balances
-- =============================================================================
-- Este seed demuestra la trazabilidad completa de movimientos de almacén:
-- - SALE_EXIT: Salidas por ventas (facturas confirmadas)
-- - CANCEL_ENTRY: Entradas por cancelaciones
-- - RETURN_ENTRY: Entradas por devoluciones
-- - TRANSFER_IN/OUT: Transferencias entre almacenes
-- - ENTRY/EXIT: Operaciones manuales
--
-- También incluye saldos a favor de clientes generados por devoluciones
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM stock_movements;
DELETE FROM warehouse_stock;
DELETE FROM customer_balances;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- Variables para IDs existentes
-- =============================================================================
SET @wh1 = UNHEX('b0000000000000000000000000000001'); -- ALM-CCS-01 (Caracas)
SET @wh2 = UNHEX('b0000000000000000000000000000002'); -- ALM-MRB-01 (Maracaibo)
SET @wh3 = UNHEX('b0000000000000000000000000000003'); -- ALM-VLC-01 (Valencia)

SET @prod1 = UNHEX('e0000000000000000000000000000001'); -- ELEC-001: Cable UTP Cat6
SET @prod2 = UNHEX('e0000000000000000000000000000002'); -- ELEC-002: Toma corriente doble
SET @prod3 = UNHEX('e0000000000000000000000000000003'); -- ELEC-003: Breaker 20A
SET @prod4 = UNHEX('e0000000000000000000000000000004'); -- ELEC-004: Multímetro digital
SET @prod5 = UNHEX('e0000000000000000000000000000005'); -- HERR-001: Taladro percutor

SET @inv2 = UNHEX('0F100000000000000000000000000002'); -- INV-2026-00027 (ImpSur)
SET @inv3 = UNHEX('0F100000000000000000000000000003'); -- INV-2026-00028 (ComNorte)
SET @inv5 = UNHEX('0F100000000000000000000000000005'); -- INV-2026-00030 (SumZulia)

SET @ord1 = UNHEX('aa000000000000000000000000000001'); -- ORD-2026-0001 (DistCaracas)
SET @ord3 = UNHEX('aa000000000000000000000000000003'); -- ORD-2026-0003 (TecSol)

SET @ret1 = UNHEX('C1000000000000000000000000000001'); -- RET-2025-0001
SET @ret2 = UNHEX('C1000000000000000000000000000002'); -- RET-2025-0002
SET @ret3 = UNHEX('C1000000000000000000000000000003'); -- RET-2025-0003

SET @cust1 = UNHEX('c1000000000000000000000000000001'); -- DistCaracas
SET @cust2 = UNHEX('c1000000000000000000000000000002'); -- ImpSur
SET @cust3 = UNHEX('c1000000000000000000000000000003'); -- ComNorte
SET @cust5 = UNHEX('c1000000000000000000000000000005'); -- SumZulia
SET @cust7 = UNHEX('c1000000000000000000000000000007'); -- TecSol

-- =============================================================================
-- WAREHOUSE STOCK INICIAL
-- =============================================================================
-- Almacén Principal Caracas (ALM-CCS-01)
INSERT INTO warehouse_stock (id, warehouse_id, product_id, quantity, reserved, last_movement_at)
VALUES
    (UNHEX('F0000000000000000000000000000001'), @wh1, @prod1, 1000, 50, NOW()),
    (UNHEX('F0000000000000000000000000000002'), @wh1, @prod2, 500, 10, NOW()),
    (UNHEX('F0000000000000000000000000000003'), @wh1, @prod3, 200, 4, NOW()),
    (UNHEX('F0000000000000000000000000000004'), @wh1, @prod4, 50, 1, NOW()),
    (UNHEX('F0000000000000000000000000000005'), @wh1, @prod5, 30, 2, NOW());

-- Almacén Maracaibo (ALM-MRB-01)
INSERT INTO warehouse_stock (id, warehouse_id, product_id, quantity, reserved, last_movement_at)
VALUES
    (UNHEX('F0000000000000000000000000000011'), @wh2, @prod1, 800, 0, NOW()),
    (UNHEX('F0000000000000000000000000000012'), @wh2, @prod2, 300, 0, NOW()),
    (UNHEX('F0000000000000000000000000000013'), @wh2, @prod3, 150, 0, NOW()),
    (UNHEX('F0000000000000000000000000000014'), @wh2, @prod4, 25, 0, NOW());

-- Almacén Valencia (ALM-VLC-01)
INSERT INTO warehouse_stock (id, warehouse_id, product_id, quantity, reserved, last_movement_at)
VALUES
    (UNHEX('F0000000000000000000000000000021'), @wh3, @prod1, 600, 0, NOW()),
    (UNHEX('F0000000000000000000000000000022'), @wh3, @prod2, 200, 0, NOW()),
    (UNHEX('F0000000000000000000000000000023'), @wh3, @prod3, 100, 0, NOW()),
    (UNHEX('F0000000000000000000000000000024'), @wh3, @prod5, 15, 0, NOW());

-- =============================================================================
-- MOVIMIENTOS DE ALMACÉN CON TRAZABILIDAD COMPLETA
-- =============================================================================

-- 1. ENTRADA MANUAL DE STOCK (ENTRY con referencia MANUAL)
INSERT INTO stock_movements (id, warehouse_id, product_id, movement_type, quantity, previous_stock, new_stock, reference_type, reference_id, reference_number, notes, created_at)
VALUES
    (UNHEX('D0000000000000000000000000000001'), @wh1, @prod1, 'ENTRY', 500, 500, 1000, 'MANUAL', NULL, NULL, 'Entrada inicial de inventario', '2026-01-15 09:00:00'),
    (UNHEX('D0000000000000000000000000000002'), @wh1, @prod2, 'ENTRY', 300, 200, 500, 'MANUAL', NULL, NULL, 'Reposición de stock', '2026-01-20 14:30:00');

-- 2. SALIDA POR VENTA (SALE_EXIT con referencia a factura)
INSERT INTO stock_movements (id, warehouse_id, product_id, movement_type, quantity, previous_stock, new_stock, reference_type, reference_id, reference_number, notes, created_at)
VALUES
    -- Factura INV-2026-00027 (ImpSur) - 30 breakers + 10 multímetros
    (UNHEX('D0000000000000000000000000000010'), @wh2, @prod3, 'SALE_EXIT', 30, 180, 150, 'INVOICE', @inv2, 'INV-2026-00027', 'Factura confirmada', '2026-02-10 11:20:00'),
    (UNHEX('D0000000000000000000000000000011'), @wh2, @prod4, 'SALE_EXIT', 10, 35, 25, 'INVOICE', @inv2, 'INV-2026-00027', 'Factura confirmada', '2026-02-10 11:20:00'),
    
    -- Factura INV-2026-00028 (ComNorte) - 5 taladros + 500 cables + 150 tomas + 50 breakers
    (UNHEX('D0000000000000000000000000000012'), @wh3, @prod5, 'SALE_EXIT', 5, 20, 15, 'INVOICE', @inv3, 'INV-2026-00028', 'Factura confirmada', '2026-02-15 16:45:00'),
    (UNHEX('D0000000000000000000000000000013'), @wh3, @prod1, 'SALE_EXIT', 500, 1100, 600, 'INVOICE', @inv3, 'INV-2026-00028', 'Factura confirmada', '2026-02-15 16:45:00'),
    (UNHEX('D0000000000000000000000000000014'), @wh3, @prod2, 'SALE_EXIT', 150, 350, 200, 'INVOICE', @inv3, 'INV-2026-00028', 'Factura confirmada', '2026-02-15 16:45:00'),
    (UNHEX('D0000000000000000000000000000015'), @wh3, @prod3, 'SALE_EXIT', 50, 150, 100, 'INVOICE', @inv3, 'INV-2026-00028', 'Factura confirmada', '2026-02-15 16:45:00'),
    
    -- Factura INV-2026-00030 (SumZulia) - 1000 cables + 100 breakers + 8 taladros + 4 multímetros + 140 tomas
    (UNHEX('D0000000000000000000000000000016'), @wh1, @prod1, 'SALE_EXIT', 600, 1600, 1000, 'INVOICE', @inv5, 'INV-2026-00030', 'Factura confirmada', '2026-03-05 10:00:00'),
    (UNHEX('D0000000000000000000000000000017'), @wh2, @prod1, 'SALE_EXIT', 400, 1200, 800, 'INVOICE', @inv5, 'INV-2026-00030', 'Factura confirmada', '2026-03-05 10:00:00'),
    (UNHEX('D0000000000000000000000000000018'), @wh1, @prod3, 'SALE_EXIT', 100, 300, 200, 'INVOICE', @inv5, 'INV-2026-00030', 'Factura confirmada', '2026-03-05 10:00:00'),
    (UNHEX('D0000000000000000000000000000019'), @wh3, @prod5, 'SALE_EXIT', 8, 23, 15, 'INVOICE', @inv5, 'INV-2026-00030', 'Factura confirmada', '2026-03-05 10:00:00'),
    (UNHEX('D0000000000000000000000000000020'), @wh2, @prod4, 'SALE_EXIT', 4, 29, 25, 'INVOICE', @inv5, 'INV-2026-00030', 'Factura confirmada', '2026-03-05 10:00:00'),
    (UNHEX('D0000000000000000000000000000021'), @wh1, @prod2, 'SALE_EXIT', 140, 640, 500, 'INVOICE', @inv5, 'INV-2026-00030', 'Factura confirmada', '2026-03-05 10:00:00');

-- 3. ENTRADA POR DEVOLUCIÓN (RETURN_ENTRY con referencia a devolución)
INSERT INTO stock_movements (id, warehouse_id, product_id, movement_type, quantity, previous_stock, new_stock, reference_type, reference_id, reference_number, notes, created_at)
VALUES
    -- Devolución RET-2025-0001 (automática) - 100 cables + 50 tomas al almacén Caracas
    (UNHEX('D0000000000000000000000000000030'), @wh1, @prod1, 'RETURN_ENTRY', 100, 900, 1000, 'RETURN', @ret1, 'RET-2025-0001', 'Devolución automática', '2026-02-20 09:30:00'),
    (UNHEX('D0000000000000000000000000000031'), @wh1, @prod2, 'RETURN_ENTRY', 50, 450, 500, 'RETURN', @ret1, 'RET-2025-0001', 'Devolución automática', '2026-02-20 09:30:00'),
    
    -- Devolución RET-2025-0002 (manual) - 15 breakers + 5 multímetros al almacén Maracaibo
    (UNHEX('D0000000000000000000000000000032'), @wh2, @prod3, 'RETURN_ENTRY', 15, 135, 150, 'RETURN', @ret2, 'RET-2025-0002', 'Devolución parcial por defecto', '2026-02-25 14:15:00'),
    (UNHEX('D0000000000000000000000000000033'), @wh2, @prod4, 'RETURN_ENTRY', 5, 20, 25, 'RETURN', @ret2, 'RET-2025-0002', 'Devolución parcial por defecto', '2026-02-25 14:15:00'),
    
    -- Devolución RET-2025-0003 (automática) - 5 taladros + 500 cables + 150 tomas + 50 breakers al almacén Valencia
    (UNHEX('D0000000000000000000000000000034'), @wh3, @prod5, 'RETURN_ENTRY', 5, 10, 15, 'RETURN', @ret3, 'RET-2025-0003', 'Devolución automática', '2026-03-01 11:00:00'),
    (UNHEX('D0000000000000000000000000000035'), @wh3, @prod1, 'RETURN_ENTRY', 500, 100, 600, 'RETURN', @ret3, 'RET-2025-0003', 'Devolución automática', '2026-03-01 11:00:00'),
    (UNHEX('D0000000000000000000000000000036'), @wh3, @prod2, 'RETURN_ENTRY', 150, 50, 200, 'RETURN', @ret3, 'RET-2025-0003', 'Devolución automática', '2026-03-01 11:00:00'),
    (UNHEX('D0000000000000000000000000000037'), @wh3, @prod3, 'RETURN_ENTRY', 50, 50, 100, 'RETURN', @ret3, 'RET-2025-0003', 'Devolución automática', '2026-03-01 11:00:00');

-- 4. ENTRADA POR CANCELACIÓN DE PEDIDO (CANCEL_ENTRY con referencia a pedido)
INSERT INTO stock_movements (id, warehouse_id, product_id, movement_type, quantity, previous_stock, new_stock, reference_type, reference_id, reference_number, notes, created_at)
VALUES
    -- Cancelación ORD-2026-0001 (DistCaracas) - stock restaurado
    (UNHEX('D0000000000000000000000000000040'), @wh1, @prod1, 'CANCEL_ENTRY', 50, 950, 1000, 'ORDER', @ord1, 'ORD-2026-0001', 'Pedido cancelado', '2026-04-20 16:30:00'),
    (UNHEX('D0000000000000000000000000000041'), @wh1, @prod2, 'CANCEL_ENTRY', 10, 490, 500, 'ORDER', @ord1, 'ORD-2026-0001', 'Pedido cancelado', '2026-04-20 16:30:00'),
    (UNHEX('D0000000000000000000000000000042'), @wh1, @prod3, 'CANCEL_ENTRY', 4, 196, 200, 'ORDER', @ord1, 'ORD-2026-0001', 'Pedido cancelado', '2026-04-20 16:30:00');

-- 5. TRANSFERENCIA ENTRE ALMACENES (TRANSFER_OUT + TRANSFER_IN con referencia)
SET @transfer_batch = 'TRF-20260315-001';
INSERT INTO stock_movements (id, warehouse_id, product_id, movement_type, quantity, previous_stock, new_stock, transfer_batch_id, reference_type, reference_id, reference_number, notes, created_at)
VALUES
    -- Transferencia de 100 cables de Caracas a Maracaibo
    (UNHEX('D0000000000000000000000000000050'), @wh1, @prod1, 'TRANSFER_OUT', 100, 1000, 900, @transfer_batch, 'TRANSFER', NULL, @transfer_batch, 'Transferencia programada', '2026-03-15 08:00:00'),
    (UNHEX('D0000000000000000000000000000051'), @wh2, @prod1, 'TRANSFER_IN', 100, 700, 800, @transfer_batch, 'TRANSFER', NULL, @transfer_batch, 'Transferencia recibida', '2026-03-15 08:00:00'),
    
    -- Transferencia de 50 tomas de Valencia a Caracas
    (UNHEX('D0000000000000000000000000000052'), @wh3, @prod2, 'TRANSFER_OUT', 50, 200, 150, 'TRF-20260320-002', 'TRANSFER', NULL, 'TRF-20260320-002', 'Reposición de stock', '2026-03-20 10:30:00'),
    (UNHEX('D0000000000000000000000000000053'), @wh1, @prod2, 'TRANSFER_IN', 50, 450, 500, 'TRF-20260320-002', 'TRANSFER', NULL, 'TRF-20260320-002', 'Reposición recibida', '2026-03-20 10:30:00');

-- 6. AJUSTE DE INVENTARIO (ADJUSTMENT)
INSERT INTO stock_movements (id, warehouse_id, product_id, movement_type, quantity, previous_stock, new_stock, reference_type, reference_id, reference_number, notes, created_at)
VALUES
    (UNHEX('D0000000000000000000000000000060'), @wh1, @prod4, 'ADJUSTMENT', 10, 40, 50, 'MANUAL', NULL, NULL, 'Ajuste por inventario físico', '2026-04-01 09:00:00'),
    (UNHEX('D0000000000000000000000000000061'), @wh2, @prod3, 'ADJUSTMENT', 5, 145, 150, 'MANUAL', NULL, NULL, 'Corrección por error de conteo', '2026-04-05 14:00:00');

-- =============================================================================
-- SALDOS A FAVOR DE CLIENTES
-- =============================================================================
-- Clientes con saldo a favor por devoluciones o anticipos sin aplicar

INSERT INTO customer_balances (id, customer_id, total_balance)
VALUES
    (UNHEX('B0000000000000000000000000000001'), @cust2, 1725.00),
    (UNHEX('B0000000000000000000000000000002'), @cust3, 8850.00),
    (UNHEX('B0000000000000000000000000000003'), @cust1, 2000.00),
    (UNHEX('B0000000000000000000000000000004'), @cust7, 5000.00),
    (UNHEX('B0000000000000000000000000000005'), @cust5, 3500.00);

INSERT INTO balance_transactions (id, customer_id, type, amount, reference_type, reference_id, reference_number, created_by, created_at)
VALUES
    -- ImpSur: Devolución RET-2025-0002
    (UNHEX('B1000000000000000000000000000001'), @cust2, 'RETURNS', 1725.00, 'RETURNS', @ret2, 'RET-2025-0002', UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), '2026-02-25 14:20:00'),

    -- ComNorte: Devolución RET-2025-0003
    (UNHEX('B1000000000000000000000000000002'), @cust3, 'RETURNS', 8850.00, 'RETURNS', @ret3, 'RET-2025-0003', UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), '2026-03-01 11:05:00'),

    -- DistCaracas: Anticipo Payment 3
    (UNHEX('B1000000000000000000000000000003'), @cust1, 'ADVANCED', 2000.00, 'ADVANCED', UNHEX('A1000000000000000000000000000003'), 'PAY-2025-0003', UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), '2026-02-20 09:00:00'),

    -- TecSol: Anticipo Payment 4
    (UNHEX('B1000000000000000000000000000004'), @cust7, 'ADVANCED', 5000.00, 'ADVANCED', UNHEX('A1000000000000000000000000000004'), 'PAY-2025-0004', UNHEX('d3bbef222f3e5cb1ee9f9eecf053d548'), '2026-03-01 16:45:00'),

    -- SumZulia: Anticipo Payment 8
    (UNHEX('B1000000000000000000000000000005'), @cust5, 'ADVANCED', 3500.00, 'ADVANCED', UNHEX('A1000000000000000000000000000008'), 'PAY-2025-0008', UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), '2026-05-10 15:30:00');

-- =============================================================================
-- RESUMEN
-- =============================================================================
-- Total de movimientos creados:
-- - 2 ENTRY (entrada manual)
-- - 12 SALE_EXIT (salida por venta)
-- - 8 RETURN_ENTRY (entrada por devolución)
-- - 3 CANCEL_ENTRY (entrada por cancelación)
-- - 4 TRANSFER_OUT/IN (transferencias)
-- - 2 ADJUSTMENT (ajustes)
-- Total: 31 movimientos con trazabilidad completa
--
-- Saldos a favor creados:
-- - 2 por devoluciones (RETURNS)
-- - 3 por anticipos (PAYMENTS)
-- Total: 5 saldos a favor
-- =============================================================================
