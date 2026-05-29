-- =============================================================================
-- Seed: Payments & Payment Allocations (8 pagos variados)
-- =============================================================================

DELETE FROM payment_allocations;
DELETE FROM payments;

-- Payment 1: Full payment for Invoice 2 (PAID, ImpSur) - Transferencia
-- Invoice 2 subtotal: 3450.00
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000001'), 'PAY-2025-0001', UNHEX('c1000000000000000000000000000002'), 'PAYMENT', 3450.00, (SELECT id FROM payment_methods WHERE code = 'PM-0002'), 'TRF-20250115-0042', '2025-01-15', 'Pago completo factura INV-2026-00027', 'APPLIED', 3450.00, UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'Super Admin', '2025-01-15 10:30:00', '2025-01-15 10:30:00');

INSERT INTO payment_allocations (id, payment_id, invoice_id, allocated_amount)
VALUES (UNHEX('AA00000000000000000000000000001'), UNHEX('A1000000000000000000000000000001'), UNHEX('0F100000000000000000000000000002'), 3450.00);

-- Payment 2: Cancelled — was for Invoice 3 (ComNorte) - Efectivo
-- Invoice 3 was FULLY_RETURNED (AUTOMATIC return), payment was cancelled
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000002'), 'PAY-2025-0002', UNHEX('c1000000000000000000000000000003'), 'PAYMENT', 3000.00, (SELECT id FROM payment_methods WHERE code = 'PM-0001'), NULL, '2025-02-10', 'Cancelado por devolucion automatica INV-2026-00028', 'CANCELLED', 0.00, UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'Admin User', '2025-02-10 14:15:00', '2025-02-10 14:15:00');

-- Payment 3: Advance payment on account (ADVANCE) - Tarjeta de débito
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000003'), 'PAY-2025-0003', UNHEX('c1000000000000000000000000000001'), 'ADVANCE', 2000.00, (SELECT id FROM payment_methods WHERE code = 'PM-0003'), 'DEB-20250220-7891', '2025-02-20', 'Anticipo para futuras compras', 'APPLIED', 2000.00, UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), 'Operator User', '2025-02-20 09:00:00', '2025-02-20 09:00:00');

-- Payment 4: Advance not yet applied (ADVANCE, DRAFT) - Zelle
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000004'), 'PAY-2025-0004', UNHEX('c1000000000000000000000000000007'), 'ADVANCE', 5000.00, (SELECT id FROM payment_methods WHERE code = 'PM-0008'), 'ZELLE-20250301-ABC', '2025-03-01', 'Anticipo pendiente de aplicar - TecSol', 'DRAFT', 0.00, UNHEX('d3bbef222f3e5cb1ee9f9eecf053d548'), 'Sales User', '2025-03-01 16:45:00', '2025-03-01 16:45:00');

-- Payment 5: Full payment for Invoice 5 (PAID, SumZulia) - Pago móvil
-- Invoice 5 subtotal: 15600.00
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000005'), 'PAY-2025-0005', UNHEX('c1000000000000000000000000000005'), 'PAYMENT', 15600.00, (SELECT id FROM payment_methods WHERE code = 'PM-0007'), 'PM-0424-9876543-20250315', '2025-03-15', 'Pago contado factura INV-2026-00030', 'APPLIED', 15600.00, UNHEX('f5ddea444b5a7ed3cc1b1aaed175f76a'), 'Gabriel', '2025-03-15 11:20:00', '2025-03-15 11:20:00');

INSERT INTO payment_allocations (id, payment_id, invoice_id, allocated_amount)
VALUES (UNHEX('AA00000000000000000000000000005'), UNHEX('A1000000000000000000000000000005'), UNHEX('0F100000000000000000000000000005'), 15600.00);

-- Payment 6: Cancelled payment - Cheque
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000006'), 'PAY-2025-0006', UNHEX('c1000000000000000000000000000008'), 'PAYMENT', 1500.00, (SELECT id FROM payment_methods WHERE code = 'PM-0005'), 'CHK-2025-0456', '2025-04-05', 'Cheque sin fondos - cancelado', 'CANCELLED', 0.00, UNHEX('a0eebc999c0b4ef8bb6d6bb9bd380215'), 'Super Admin', '2025-04-05 08:30:00', '2025-04-05 08:30:00');

-- Payment 7: Cancelled — was for Invoice 3 (ComNorte) - Tarjeta de crédito
-- Invoice 3 was FULLY_RETURNED (AUTOMATIC return), payment was cancelled
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000007'), 'PAY-2025-0007', UNHEX('c1000000000000000000000000000003'), 'PAYMENT', 2000.00, (SELECT id FROM payment_methods WHERE code = 'PM-0004'), 'CC-VISA-2025-4567-8901', '2025-04-20', 'Cancelado por devolucion automatica INV-2026-00028', 'CANCELLED', 0.00, UNHEX('b1ffcd000d1c4fa9bc7e7ccace31b326'), 'Admin User', '2025-04-20 13:00:00', '2025-04-20 13:00:00');

-- Payment 8: Another advance - Divisas (USD)
INSERT INTO payments (id, payment_number, customer_id, type, amount, payment_method_id, reference_number, payment_date, notes, status, applied_amount, created_by, created_by_name, created_at, updated_at)
VALUES (UNHEX('A1000000000000000000000000000008'), 'PAY-2025-0008', UNHEX('c1000000000000000000000000000005'), 'ADVANCE', 3500.00, (SELECT id FROM payment_methods WHERE code = 'PM-0006'), 'USD-CASH-20250510', '2025-05-10', 'Anticipo en divisas - SumZulia', 'APPLIED', 3500.00, UNHEX('c2aade111e2d4ba0dd8f8ddbdf42c437'), 'Operator User', '2025-05-10 15:30:00', '2025-05-10 15:30:00');

-- Update sequence
UPDATE payment_number_sequence SET next_value = 9 WHERE year = 2025;
INSERT IGNORE INTO payment_number_sequence (year, next_value) VALUES (2025, 9);
