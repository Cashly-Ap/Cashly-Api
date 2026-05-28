SET @inv1 = (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00026');
SET @inv2 = (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00027');
SET @inv3 = (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00028');
SET @inv4 = (SELECT id FROM invoices WHERE invoice_number = 'INV-2026-00029');

SET @invLine1 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv1 AND product_code = 'ELEC-001');
SET @invLine2 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv1 AND product_code = 'ELEC-002');
SET @invLine3 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv2 AND product_code = 'ELEC-003');
SET @invLine4 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv2 AND product_code = 'ELEC-004');
SET @invLine5 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv3 AND product_code = 'HERR-001');
SET @invLine6 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv3 AND product_code = 'ELEC-001');
SET @invLine7 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv3 AND product_code = 'ELEC-002');
SET @invLine8 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv3 AND product_code = 'ELEC-003');
SET @invLine9 = (SELECT id FROM invoice_lines WHERE invoice_id = @inv4 AND product_code = 'HERR-001');

SET @cust1 = (SELECT id FROM customers WHERE code = 1001);
SET @cust2 = (SELECT id FROM customers WHERE code = 1002);
SET @cust3 = (SELECT id FROM customers WHERE code = 1003);
SET @cust4 = (SELECT id FROM customers WHERE code = 1004);

SET @custName1 = (SELECT name FROM customers WHERE code = 1001);
SET @custName2 = (SELECT name FROM customers WHERE code = 1002);
SET @custName3 = (SELECT name FROM customers WHERE code = 1003);
SET @custName4 = (SELECT name FROM customers WHERE code = 1004);

SET @custRif1 = (SELECT rif FROM customers WHERE code = 1001);
SET @custRif2 = (SELECT rif FROM customers WHERE code = 1002);
SET @custRif3 = (SELECT rif FROM customers WHERE code = 1003);
SET @custRif4 = (SELECT rif FROM customers WHERE code = 1004);

SET @prod1 = (SELECT id FROM products WHERE code = 'ELEC-001');
SET @prod2 = (SELECT id FROM products WHERE code = 'ELEC-002');
SET @prod3 = (SELECT id FROM products WHERE code = 'ELEC-003');
SET @prod4 = (SELECT id FROM products WHERE code = 'ELEC-004');
SET @prod5 = (SELECT id FROM products WHERE code = 'HERR-001');

SET @wh1 = (SELECT id FROM warehouses WHERE code = 'ALM-CCS-01');
SET @wh2 = (SELECT id FROM warehouses WHERE code = 'ALM-MRB-01');
SET @wh3 = (SELECT id FROM warehouses WHERE code = 'ALM-VLC-01');

SET @user1 = (SELECT id FROM users WHERE user_name = 'Super Admin');
SET @user2 = (SELECT id FROM users WHERE user_name = 'Admin User');

INSERT INTO returns (id, return_number, invoice_id, customer_id, customer_name, customer_rif, return_type, reason, total, created_by, created_by_name, created_at, updated_at)
VALUES
    (UNHEX('C1000000000000000000000000000001'), 'RET-2025-0001', @inv1, @cust1, @custName1, @custRif1, 'AUTOMATIC', 'Devolucion automatica por factura cancelada - items completos', 1250.00, @user1, 'Super Admin', NOW(), NOW()),
    (UNHEX('C1000000000000000000000000000002'), 'RET-2025-0002', @inv2, @cust2, @custName2, @custRif2, 'MANUAL', 'Devolucion parcial - cliente reporta defecto en productos', 1725.00, @user2, 'Admin User', NOW(), NOW()),
    (UNHEX('C1000000000000000000000000000003'), 'RET-2025-0003', @inv3, @cust3, @custName3, @custRif3, 'AUTOMATIC', 'Devolucion automatica por factura cancelada - orden no procesada', 8850.00, @user1, 'Super Admin', NOW(), NOW()),
    (UNHEX('C1000000000000000000000000000004'), 'RET-2025-0004', @inv4, @cust4, @custName4, @custRif4, 'MANUAL', 'Devolucion parcial - cantidades especificas solicitadas por cliente', 350.00, @user2, 'Admin User', NOW(), NOW());

INSERT INTO return_lines (id, return_id, invoice_line_id, product_id, quantity, unit_price, line_total)
VALUES
    (UNHEX('C2000000000000000000000000000001'), UNHEX('C1000000000000000000000000000001'), @invLine1, @prod1, 100.0000, 5.50, 550.00),
    (UNHEX('C2000000000000000000000000000002'), UNHEX('C1000000000000000000000000000001'), @invLine2, @prod2, 50.0000, 14.00, 700.00),
    (UNHEX('C2000000000000000000000000000003'), UNHEX('C1000000000000000000000000000002'), @invLine3, @prod3, 15.0000, 45.00, 675.00),
    (UNHEX('C2000000000000000000000000000004'), UNHEX('C1000000000000000000000000000002'), @invLine4, @prod4, 5.0000, 210.00, 1050.00),
    (UNHEX('C2000000000000000000000000000005'), UNHEX('C1000000000000000000000000000003'), @invLine5, @prod5, 5.0000, 350.00, 1750.00),
    (UNHEX('C2000000000000000000000000000006'), UNHEX('C1000000000000000000000000000003'), @invLine6, @prod1, 500.0000, 5.50, 2750.00),
    (UNHEX('C2000000000000000000000000000007'), UNHEX('C1000000000000000000000000000003'), @invLine7, @prod2, 150.0000, 14.00, 2100.00),
    (UNHEX('C2000000000000000000000000000008'), UNHEX('C1000000000000000000000000000003'), @invLine8, @prod3, 50.0000, 45.00, 2250.00),
    (UNHEX('C2000000000000000000000000000009'), UNHEX('C1000000000000000000000000000004'), @invLine9, @prod5, 1.0000, 350.00, 350.00);

INSERT INTO return_line_allocations (id, return_line_id, warehouse_id, allocated)
VALUES
    (UNHEX('C3000000000000000000000000000001'), UNHEX('C2000000000000000000000000000001'), @wh1, 100.0000),
    (UNHEX('C3000000000000000000000000000002'), UNHEX('C2000000000000000000000000000002'), @wh1, 50.0000),
    (UNHEX('C3000000000000000000000000000003'), UNHEX('C2000000000000000000000000000003'), @wh2, 15.0000),
    (UNHEX('C3000000000000000000000000000004'), UNHEX('C2000000000000000000000000000004'), @wh2, 5.0000),
    (UNHEX('C3000000000000000000000000000005'), UNHEX('C2000000000000000000000000000005'), @wh3, 5.0000),
    (UNHEX('C3000000000000000000000000000006'), UNHEX('C2000000000000000000000000000006'), @wh3, 500.0000),
    (UNHEX('C3000000000000000000000000000007'), UNHEX('C2000000000000000000000000000007'), @wh3, 150.0000),
    (UNHEX('C3000000000000000000000000000008'), UNHEX('C2000000000000000000000000000008'), @wh3, 50.0000),
    (UNHEX('C3000000000000000000000000000009'), UNHEX('C2000000000000000000000000000009'), @wh1, 1.0000);

INSERT INTO return_number_sequence (year, next_value) VALUES (2025, 5);
