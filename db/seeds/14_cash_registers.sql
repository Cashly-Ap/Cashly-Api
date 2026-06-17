-- =============================================================================
-- Seed: Cash Registers (Cajas) — June 2026
--
-- Crea registros de caja diarios para 10 días de junio 2026.
-- Cada registro representa la apertura (OPEN) o cierre (CLOSED) de caja,
-- con desglose por método de pago en cash_register_details.
--
-- Estados:
--   OPEN   = Caja abierta (aún no cerrada, típicamente el día actual)
--   CLOSED = Caja cerrada con totales reconciliados
--
-- Consistencia:
--   expected_closing_balance = total_payments + total_advances
--                              + total_debit_notes - total_returns
--                              - total_credit_notes
--
-- Conexiones:
--   - payment_method_id → payment_methods (FK, busqueda por código)
--   - opened_by / closed_by → users (FK, UUIDs fijos del seed)
--   - closure_id → cash_registers (FK con ON DELETE CASCADE)
--
-- Metodos de pago usados (todos los 8 existentes):
--   PM-0001 Efectivo
--   PM-0002 Transferencia bancaria
--   PM-0003 Tarjeta de débito
--   PM-0004 Tarjeta de crédito
--   PM-0005 Cheque
--   PM-0006 Divisas
--   PM-0007 Pago móvil
--   PM-0008 Zelle
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM cash_register_details;
DELETE FROM cash_registers;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- Variables: Payment Methods (búsqueda por código, UUIDs dinámicos)
-- =============================================================================
SET @pm_efectivo   = (SELECT id FROM payment_methods WHERE code = 'PM-0001');
SET @pm_transf     = (SELECT id FROM payment_methods WHERE code = 'PM-0002');
SET @pm_debito     = (SELECT id FROM payment_methods WHERE code = 'PM-0003');
SET @pm_credito    = (SELECT id FROM payment_methods WHERE code = 'PM-0004');
SET @pm_cheque     = (SELECT id FROM payment_methods WHERE code = 'PM-0005');
SET @pm_divisas    = (SELECT id FROM payment_methods WHERE code = 'PM-0006');
SET @pm_pago_movil = (SELECT id FROM payment_methods WHERE code = 'PM-0007');
SET @pm_zelle      = (SELECT id FROM payment_methods WHERE code = 'PM-0008');

-- =============================================================================
-- Variables: Users (UUIDs fijos del seed 01_users)
-- =============================================================================
SET @usr_gabriel  = UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a');
SET @usr_super    = UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215');
SET @usr_admin    = UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326');
SET @usr_operator = UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437');
SET @usr_sales    = UNHEX('d3bbef222f3e5cb1ee9f9eecf053d548');

-- =============================================================================
-- CASH REGISTERS
-- =============================================================================
-- Columnas: id, closure_date, status, opened_by, opened_by_name, opened_at,
--           closed_by, closed_by_name, closed_at,
--           total_payments, total_advances, total_returns,
--           total_credit_notes, total_debit_notes, total_invoices,
--           payment_count, advance_count, return_count, invoice_count,
--           expected_closing_balance, actual_closing_balance, difference,
--           closing_notes, version, created_at, updated_at
-- =============================================================================

INSERT INTO cash_registers (id, closure_date, status, opened_by, opened_by_name, opened_at, closed_by, closed_by_name, closed_at, total_payments, total_advances, total_returns, total_credit_notes, total_debit_notes, total_invoices, payment_count, advance_count, return_count, invoice_count, expected_closing_balance, actual_closing_balance, difference, closing_notes, version, created_at, updated_at)
VALUES

    -- =========================================================================
    -- 1. Lunes 1 Jun — CLOSED — Gabriel abre y cierra
    --    Ventas del día: 4 transacciones, solo pagos, sin novedades
    --    expected = 2150 + 0 + 0 - 0 - 0 = 2150
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000001'), '2026-06-01', 'CLOSED',
     @usr_gabriel, 'Gabriel', '2026-06-01 07:30:00',
     @usr_gabriel, 'Gabriel', '2026-06-01 17:30:00',
     2150.00, 0.00, 0.00, 0.00, 0.00, 2150.00,
     4, 0, 0, 4, 2150.00, 2150.00, 0.00,
     'Cierre normal del día, sin incidencias', 0,
     '2026-06-01 07:30:00', '2026-06-01 17:30:00'),

    -- =========================================================================
    -- 2. Martes 2 Jun — CLOSED — Super Admin abre y cierra
    --    Incluye un adelanto de cliente en efectivo
    --    expected = 4200 + 500 + 0 - 0 - 0 = 4700
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000002'), '2026-06-02', 'CLOSED',
     @usr_super, 'Super Admin', '2026-06-02 08:00:00',
     @usr_super, 'Super Admin', '2026-06-02 17:00:00',
     4200.00, 500.00, 0.00, 0.00, 0.00, 4200.00,
     3, 1, 0, 3, 4700.00, 4700.00, 0.00,
     'Adelanto recibido de cliente para futuras compras', 0,
     '2026-06-02 08:00:00', '2026-06-02 17:00:00'),

    -- =========================================================================
    -- 3. Miércoles 3 Jun — CLOSED — Gabriel abre, Operator cierra
    --    Devolución procesada + nota de crédito + nota de débito
    --    expected = 5370 + 0 + 75 - 250 - 150 = 5045
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000003'), '2026-06-03', 'CLOSED',
     @usr_gabriel, 'Gabriel', '2026-06-03 07:30:00',
     @usr_operator, 'Operator User', '2026-06-03 18:00:00',
     5370.00, 0.00, 250.00, 150.00, 75.00, 5370.00,
     5, 0, 1, 4, 5045.00, 5045.00, 0.00,
     'Devolución parcial y notas aplicadas sin discrepancias', 0,
     '2026-06-03 07:30:00', '2026-06-03 18:00:00'),

    -- =========================================================================
    -- 4. Jueves 4 Jun — CLOSED — Operator abre y cierra
    --    Adelanto en efectivo de cliente frecuente
    --    expected = 3150 + 1000 + 0 - 0 - 0 = 4150
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000004'), '2026-06-04', 'CLOSED',
     @usr_operator, 'Operator User', '2026-06-04 08:00:00',
     @usr_operator, 'Operator User', '2026-06-04 17:00:00',
     3150.00, 1000.00, 0.00, 0.00, 0.00, 3150.00,
     3, 1, 0, 3, 4150.00, 4150.00, 0.00,
     'Adelanto en efectivo de cliente frecuente', 0,
     '2026-06-04 08:00:00', '2026-06-04 17:00:00'),

    -- =========================================================================
    -- 5. Viernes 5 Jun — CLOSED — Gabriel abre y cierra (día movido)
    --    Alto volumen: 6 pagos, 1 devolución, 1 nota de débito
    --    expected = 8920 + 0 + 120 - 350 - 0 = 8690
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000005'), '2026-06-05', 'CLOSED',
     @usr_gabriel, 'Gabriel', '2026-06-05 07:00:00',
     @usr_gabriel, 'Gabriel', '2026-06-05 18:30:00',
     8920.00, 0.00, 350.00, 0.00, 120.00, 8920.00,
     6, 0, 1, 5, 8690.00, 8690.00, 0.00,
     'Viernes de alto volumen, nota de débito por recargo aplicada', 0,
     '2026-06-05 07:00:00', '2026-06-05 18:30:00'),

    -- =========================================================================
    -- 6. Lunes 8 Jun — CLOSED — Super Admin abre y cierra
    --    Adelanto en divisas + nota de crédito aplicada
    --    expected = 4100 + 2000 + 0 - 500 - 0 = 5600
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000006'), '2026-06-08', 'CLOSED',
     @usr_super, 'Super Admin', '2026-06-08 08:00:00',
     @usr_super, 'Super Admin', '2026-06-08 17:00:00',
     4100.00, 2000.00, 0.00, 500.00, 0.00, 4100.00,
     3, 1, 0, 3, 5600.00, 5600.00, 0.00,
     'Adelanto en divisas y nota de crédito por ajuste', 0,
     '2026-06-08 08:00:00', '2026-06-08 17:00:00'),

    -- =========================================================================
    -- 7. Miércoles 10 Jun — CLOSED — Gabriel abre, Admin cierra
    --    Adelanto en divisas + nota de crédito
    --    expected = 6450 + 1500 + 0 - 200 - 0 = 7750
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000007'), '2026-06-10', 'CLOSED',
     @usr_gabriel, 'Gabriel', '2026-06-10 07:30:00',
     @usr_admin, 'Admin User', '2026-06-10 17:30:00',
     6450.00, 1500.00, 0.00, 200.00, 0.00, 6450.00,
     4, 1, 0, 4, 7750.00, 7750.00, 0.00,
     'Cierre sin novedades, nota de crédito aplicada', 0,
     '2026-06-10 07:30:00', '2026-06-10 17:30:00'),

    -- =========================================================================
    -- 8. Jueves 11 Jun — CLOSED — Operator abre y cierra
    --    Devolución procesada sin incidencias
    --    expected = 2800 + 0 + 0 - 500 - 0 = 2300
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000008'), '2026-06-11', 'CLOSED',
     @usr_operator, 'Operator User', '2026-06-11 08:00:00',
     @usr_operator, 'Operator User', '2026-06-11 17:00:00',
     2800.00, 0.00, 500.00, 0.00, 0.00, 2800.00,
     3, 0, 1, 3, 2300.00, 2300.00, 0.00,
     'Devolución procesada, saldo reconciliado', 0,
     '2026-06-11 08:00:00', '2026-06-11 17:00:00'),

    -- =========================================================================
    -- 9. Lunes 15 Jun — CLOSED — Gabriel abre y cierra (día pesado)
    --    Alto volumen: 5 pagos + 2 adelantos + 1 devolución
    --    expected = 12800 + 3000 + 0 - 1200 - 0 = 14600
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000009'), '2026-06-15', 'CLOSED',
     @usr_gabriel, 'Gabriel', '2026-06-15 07:30:00',
     @usr_gabriel, 'Gabriel', '2026-06-15 18:00:00',
     12800.00, 3000.00, 1200.00, 0.00, 0.00, 12800.00,
     5, 2, 1, 6, 14600.00, 14600.00, 0.00,
     'Día de alto volumen, devolución y adelantos múltiples', 0,
     '2026-06-15 07:30:00', '2026-06-15 18:00:00'),

    -- =========================================================================
    -- 10. Miércoles 17 Jun — OPEN — Gabriel abre (día actual)
    --     Caja aún abierta, sin cierre. actual_closing_balance = NULL
    --     expected = 3200 + 0 + 0 - 0 - 0 = 3200
    -- =========================================================================
    (UNHEX('d1000000000000000000000000000010'), '2026-06-17', 'OPEN',
     @usr_gabriel, 'Gabriel', '2026-06-17 07:00:00',
     NULL, NULL, NULL,
     3200.00, 0.00, 0.00, 0.00, 0.00, 3200.00,
     3, 0, 0, 3, 3200.00, NULL, NULL,
     'Caja del día en curso — aún abierta', 0,
     '2026-06-17 07:00:00', '2026-06-17 07:00:00');

-- =============================================================================
-- CASH REGISTER DETAILS
-- =============================================================================
-- Columnas: id, closure_id, payment_method_id, payment_method_name,
--           transaction_count, expected_amount, actual_amount, difference
-- =============================================================================

INSERT INTO cash_register_details (id, closure_id, payment_method_id, payment_method_name, transaction_count, expected_amount, actual_amount, difference)
VALUES

    -- Registro 1: Lunes 1 Jun
    (UNHEX('d2000000000000000000000000000001'), UNHEX('d1000000000000000000000000000001'), @pm_efectivo,   'Efectivo',              1,   200.00,   200.00, 0.00),
    (UNHEX('d2000000000000000000000000000002'), UNHEX('d1000000000000000000000000000001'), @pm_transf,     'Transferencia bancaria', 1,  1500.00,  1500.00, 0.00),
    (UNHEX('d2000000000000000000000000000003'), UNHEX('d1000000000000000000000000000001'), @pm_debito,     'Tarjeta de débito',      1,   350.00,   350.00, 0.00),
    (UNHEX('d2000000000000000000000000000004'), UNHEX('d1000000000000000000000000000001'), @pm_pago_movil, 'Pago móvil',             1,   100.00,   100.00, 0.00),
    -- Subtotal pagos: 2150.00 ✓

    -- Registro 2: Martes 2 Jun
    (UNHEX('d2000000000000000000000000000005'), UNHEX('d1000000000000000000000000000002'), @pm_transf,     'Transferencia bancaria', 1,  2800.00,  2800.00, 0.00),
    (UNHEX('d2000000000000000000000000000006'), UNHEX('d1000000000000000000000000000002'), @pm_cheque,     'Cheque',                 1,   950.00,   950.00, 0.00),
    (UNHEX('d2000000000000000000000000000007'), UNHEX('d1000000000000000000000000000002'), @pm_pago_movil, 'Pago móvil',             1,   450.00,   450.00, 0.00),
    (UNHEX('d2000000000000000000000000000008'), UNHEX('d1000000000000000000000000000002'), @pm_efectivo,   'Efectivo (adelanto)',    1,   500.00,   500.00, 0.00),
    -- Subtotal pagos: 4200.00, adelanto: 500.00 ✓

    -- Registro 3: Miércoles 3 Jun
    (UNHEX('d2000000000000000000000000000009'), UNHEX('d1000000000000000000000000000003'), @pm_efectivo,   'Efectivo',              1,   650.00,   650.00, 0.00),
    (UNHEX('d2000000000000000000000000000010'), UNHEX('d1000000000000000000000000000003'), @pm_debito,     'Tarjeta de débito',      1,   420.00,   420.00, 0.00),
    (UNHEX('d2000000000000000000000000000011'), UNHEX('d1000000000000000000000000000003'), @pm_credito,    'Tarjeta de crédito',     1,  1800.00,  1800.00, 0.00),
    (UNHEX('d2000000000000000000000000000012'), UNHEX('d1000000000000000000000000000003'), @pm_divisas,    'Divisas',                1,   300.00,   300.00, 0.00),
    (UNHEX('d2000000000000000000000000000013'), UNHEX('d1000000000000000000000000000003'), @pm_zelle,      'Zelle',                  1,  2200.00,  2200.00, 0.00),
    -- Subtotal pagos: 5370.00 ✓

    -- Registro 4: Jueves 4 Jun
    (UNHEX('d2000000000000000000000000000014'), UNHEX('d1000000000000000000000000000004'), @pm_transf,     'Transferencia bancaria', 1,  1800.00,  1800.00, 0.00),
    (UNHEX('d2000000000000000000000000000015'), UNHEX('d1000000000000000000000000000004'), @pm_pago_movil, 'Pago móvil',             1,   850.00,   850.00, 0.00),
    (UNHEX('d2000000000000000000000000000016'), UNHEX('d1000000000000000000000000000004'), @pm_cheque,     'Cheque',                 1,   500.00,   500.00, 0.00),
    (UNHEX('d2000000000000000000000000000017'), UNHEX('d1000000000000000000000000000004'), @pm_efectivo,   'Efectivo (adelanto)',    1,  1000.00,  1000.00, 0.00),
    -- Subtotal pagos: 3150.00, adelanto: 1000.00 ✓

    -- Registro 5: Viernes 5 Jun (día movido)
    (UNHEX('d2000000000000000000000000000018'), UNHEX('d1000000000000000000000000000005'), @pm_efectivo,   'Efectivo',              2,  1250.00,  1250.00, 0.00),
    (UNHEX('d2000000000000000000000000000019'), UNHEX('d1000000000000000000000000000005'), @pm_transf,     'Transferencia bancaria', 1,  3200.00,  3200.00, 0.00),
    (UNHEX('d2000000000000000000000000000020'), UNHEX('d1000000000000000000000000000005'), @pm_debito,     'Tarjeta de débito',      1,   780.00,   780.00, 0.00),
    (UNHEX('d2000000000000000000000000000021'), UNHEX('d1000000000000000000000000000005'), @pm_credito,    'Tarjeta de crédito',     1,  2500.00,  2500.00, 0.00),
    (UNHEX('d2000000000000000000000000000022'), UNHEX('d1000000000000000000000000000005'), @pm_zelle,      'Zelle',                  1,  1190.00,  1190.00, 0.00),
    -- Subtotal pagos: 8920.00 ✓

    -- Registro 6: Lunes 8 Jun
    (UNHEX('d2000000000000000000000000000023'), UNHEX('d1000000000000000000000000000006'), @pm_transf,     'Transferencia bancaria', 1,  2500.00,  2500.00, 0.00),
    (UNHEX('d2000000000000000000000000000024'), UNHEX('d1000000000000000000000000000006'), @pm_pago_movil, 'Pago móvil',             1,  1100.00,  1100.00, 0.00),
    (UNHEX('d2000000000000000000000000000025'), UNHEX('d1000000000000000000000000000006'), @pm_debito,     'Tarjeta de débito',      1,   500.00,   500.00, 0.00),
    (UNHEX('d2000000000000000000000000000026'), UNHEX('d1000000000000000000000000000006'), @pm_divisas,    'Divisas (adelanto)',     1,  2000.00,  2000.00, 0.00),
    -- Subtotal pagos: 4100.00, adelanto: 2000.00 ✓

    -- Registro 7: Miércoles 10 Jun
    (UNHEX('d2000000000000000000000000000027'), UNHEX('d1000000000000000000000000000007'), @pm_efectivo,   'Efectivo',              1,   800.00,   800.00, 0.00),
    (UNHEX('d2000000000000000000000000000028'), UNHEX('d1000000000000000000000000000007'), @pm_credito,    'Tarjeta de crédito',     1,  3200.00,  3200.00, 0.00),
    (UNHEX('d2000000000000000000000000000029'), UNHEX('d1000000000000000000000000000007'), @pm_transf,     'Transferencia bancaria', 1,  1800.00,  1800.00, 0.00),
    (UNHEX('d2000000000000000000000000000030'), UNHEX('d1000000000000000000000000000007'), @pm_zelle,      'Zelle',                  1,   650.00,   650.00, 0.00),
    (UNHEX('d2000000000000000000000000000031'), UNHEX('d1000000000000000000000000000007'), @pm_divisas,    'Divisas (adelanto)',     1,  1500.00,  1500.00, 0.00),
    -- Subtotal pagos: 6450.00, adelanto: 1500.00 ✓

    -- Registro 8: Jueves 11 Jun
    (UNHEX('d2000000000000000000000000000032'), UNHEX('d1000000000000000000000000000008'), @pm_efectivo,   'Efectivo',              1,   950.00,   950.00, 0.00),
    (UNHEX('d2000000000000000000000000000033'), UNHEX('d1000000000000000000000000000008'), @pm_pago_movil, 'Pago móvil',             1,   750.00,   750.00, 0.00),
    (UNHEX('d2000000000000000000000000000034'), UNHEX('d1000000000000000000000000000008'), @pm_cheque,     'Cheque',                 1,  1100.00,  1100.00, 0.00),
    -- Subtotal pagos: 2800.00 ✓

    -- Registro 9: Lunes 15 Jun (día pesado)
    (UNHEX('d2000000000000000000000000000035'), UNHEX('d1000000000000000000000000000009'), @pm_transf,     'Transferencia bancaria', 2,  5500.00,  5500.00, 0.00),
    (UNHEX('d2000000000000000000000000000036'), UNHEX('d1000000000000000000000000000009'), @pm_credito,    'Tarjeta de crédito',     1,  3800.00,  3800.00, 0.00),
    (UNHEX('d2000000000000000000000000000037'), UNHEX('d1000000000000000000000000000009'), @pm_efectivo,   'Efectivo',              1,  2500.00,  2500.00, 0.00),
    (UNHEX('d2000000000000000000000000000038'), UNHEX('d1000000000000000000000000000009'), @pm_zelle,      'Zelle',                  1,  1000.00,  1000.00, 0.00),
    (UNHEX('d2000000000000000000000000000039'), UNHEX('d1000000000000000000000000000009'), @pm_efectivo,   'Efectivo (adelanto)',    1,  2000.00,  2000.00, 0.00),
    (UNHEX('d2000000000000000000000000000040'), UNHEX('d1000000000000000000000000000009'), @pm_divisas,    'Divisas (adelanto)',     1,  1000.00,  1000.00, 0.00),
    -- Subtotal pagos: 12800.00, adelantos: 3000.00 ✓

    -- Registro 10: Miércoles 17 Jun — OPEN (actual_closing_balance = NULL porque aún no se cierra)
    (UNHEX('d2000000000000000000000000000041'), UNHEX('d1000000000000000000000000000010'), @pm_efectivo,   'Efectivo',              1,   700.00,   NULL, NULL),
    (UNHEX('d2000000000000000000000000000042'), UNHEX('d1000000000000000000000000000010'), @pm_debito,     'Tarjeta de débito',      1,   500.00,   NULL, NULL),
    (UNHEX('d2000000000000000000000000000043'), UNHEX('d1000000000000000000000000000010'), @pm_transf,     'Transferencia bancaria', 1,  2000.00,   NULL, NULL);
    -- Subtotal pagos: 3200.00 (esperado, aún pendiente de cierre) ✓

-- =============================================================================
-- VERIFICACIÓN DE CONSISTENCIA
-- =============================================================================
-- Total registros de caja: 10
--   - 9 CLOSED (1, 2, 3, 4, 5, 8, 10, 11, 15 Jun)
--   - 1 OPEN   (17 Jun — día actual)
--
-- Total detalles: 43 líneas
--   - Reg 1:  4 detalles =  2150.00 en pagos
--   - Reg 2:  4 detalles =  4200.00 pagos +  500.00 adelanto
--   - Reg 3:  5 detalles =  5370.00 en pagos
--   - Reg 4:  4 detalles =  3150.00 pagos + 1000.00 adelanto
--   - Reg 5:  5 detalles =  8920.00 en pagos
--   - Reg 6:  4 detalles =  4100.00 pagos + 2000.00 adelanto
--   - Reg 7:  5 detalles =  6450.00 pagos + 1500.00 adelanto
--   - Reg 8:  3 detalles =  2800.00 en pagos
--   - Reg 9:  6 detalles = 12800.00 pagos + 3000.00 adelantos
--   - Reg 10: 3 detalles =  3200.00 en pagos (pendiente cierre)
--
-- Todos los 8 métodos de pago aparecen al menos una vez:
--   PM-0001 Efectivo         → Regs 1,2,3,4,5,7,8,9,10 (9 apariciones)
--   PM-0002 Transferencia    → Regs 1,2,4,5,6,7,9,10  (8 apariciones)
--   PM-0003 Tarjeta débito   → Regs 1,3,5,6,10         (5 apariciones)
--   PM-0004 Tarjeta crédito  → Regs 3,5,7,9            (4 apariciones)
--   PM-0005 Cheque           → Regs 2,4,8              (3 apariciones)
--   PM-0006 Divisas          → Regs 3,6,7,9            (4 apariciones)
--   PM-0007 Pago móvil       → Regs 1,2,4,6,8          (5 apariciones)
--   PM-0008 Zelle            → Regs 3,5,7,9            (4 apariciones)
-- =============================================================================
