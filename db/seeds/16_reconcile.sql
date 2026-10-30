-- Derived data: document references remain the single source of truth.
-- Sales represented as returned invoices had already exited stock before their return.
INSERT INTO stock_movements (id,warehouse_id,product_id,movement_type,quantity,previous_stock,new_stock,reference_type,reference_id,reference_number,notes,created_at)
SELECT UNHEX(MD5(CONCAT('invoice:',HEX(a.id)))),a.warehouse_id,l.product_id,'SALE_EXIT',a.allocated,0,0,'INVOICE',i.id,i.invoice_number,'Seed confirmed sale',i.created_at
FROM invoice_line_allocations a JOIN invoice_lines l ON l.id=a.invoice_line_id JOIN invoices i ON i.id=l.invoice_id
WHERE i.status NOT IN ('DRAFT','CANCELLED');
INSERT INTO stock_movements (id,warehouse_id,product_id,movement_type,quantity,previous_stock,new_stock,reference_type,reference_id,reference_number,notes,created_at)
SELECT UNHEX(MD5(CONCAT('return:',HEX(a.id)))),a.warehouse_id,l.product_id,'RETURN_ENTRY',a.allocated,0,0,'RETURN',r.id,r.return_number,'Seed returned stock',DATE_ADD(r.created_at,INTERVAL 1 SECOND)
FROM return_line_allocations a JOIN return_lines l ON l.id=a.return_line_id JOIN returns r ON r.id=l.return_id;
INSERT INTO stock_movements (id,warehouse_id,product_id,movement_type,quantity,previous_stock,new_stock,reference_type,reference_id,reference_number,notes,created_at)
SELECT UNHEX(MD5(CONCAT('purchase:',HEX(a.id)))),a.warehouse_id,l.product_id,'ENTRY',a.quantity,0,0,'PURCHASE',p.id,p.purchase_number,'Seed confirmed purchase',p.confirmed_at
FROM purchase_line_allocations a JOIN purchase_lines l ON l.id=a.purchase_line_id JOIN purchases p ON p.id=l.purchase_id WHERE p.status='CONFIRMED';

CREATE TEMPORARY TABLE seed_stock_running AS
SELECT id,
 SUM(CASE WHEN movement_type IN ('SALE_EXIT','EXIT','TRANSFER_OUT') THEN -quantity ELSE quantity END) OVER (PARTITION BY warehouse_id,product_id ORDER BY created_at,id ROWS UNBOUNDED PRECEDING) AS new_quantity,
 CASE WHEN movement_type IN ('SALE_EXIT','EXIT','TRANSFER_OUT') THEN -quantity ELSE quantity END AS delta
FROM stock_movements;
UPDATE stock_movements m JOIN seed_stock_running r ON r.id=m.id SET m.previous_stock=r.new_quantity-r.delta,m.new_stock=r.new_quantity;
DROP TEMPORARY TABLE seed_stock_running;
UPDATE warehouse_stock w SET quantity=(SELECT SUM(CASE WHEN m.movement_type IN ('SALE_EXIT','EXIT','TRANSFER_OUT') THEN -m.quantity ELSE m.quantity END) FROM stock_movements m WHERE m.warehouse_id=w.warehouse_id AND m.product_id=w.product_id),
reserved=COALESCE((SELECT SUM(a.allocated) FROM order_line_allocations a JOIN order_lines l ON l.id=a.order_line_id JOIN orders o ON o.id=l.order_id WHERE a.warehouse_id=w.warehouse_id AND l.product_id=w.product_id AND o.status='DRAFT'),0)
       +COALESCE((SELECT SUM(a.allocated) FROM invoice_line_allocations a JOIN invoice_lines l ON l.id=a.invoice_line_id JOIN invoices i ON i.id=l.invoice_id WHERE a.warehouse_id=w.warehouse_id AND l.product_id=w.product_id AND i.status='DRAFT'),0),
last_movement_at=(SELECT MAX(m.created_at) FROM stock_movements m WHERE m.warehouse_id=w.warehouse_id AND m.product_id=w.product_id);

-- An advance is exactly the unapplied part of an approved payment.
INSERT INTO balance_transactions (id,customer_id,type,amount,reference_type,reference_id,reference_number,created_by,created_at)
SELECT UNHEX(MD5(CONCAT('advance:',HEX(id)))),customer_id,'ADVANCED',amount-applied_amount,'ADVANCED',id,payment_number,created_by,created_at FROM payments WHERE status='APPLIED' AND amount>applied_amount;
INSERT INTO balance_transactions (id,customer_id,type,amount,reference_type,reference_id,reference_number,created_by,created_at)
SELECT UNHEX(MD5(CONCAT('return-balance:',HEX(id)))),customer_id,'RETURNS',total,'RETURNS',id,return_number,created_by,created_at FROM returns;
INSERT INTO customer_balances (id,customer_id,total_balance)
SELECT UNHEX(MD5(CONCAT('balance:',HEX(customer_id)))),customer_id,SUM(amount) FROM balance_transactions GROUP BY customer_id;

-- Reflect actual invoice receipts after returns; do not leave fully paid fixtures overdue.
UPDATE invoices i SET status=CASE
 WHEN COALESCE(i.returned_total,0)>=i.subtotal THEN 'FULLY_RETURNED'
 WHEN COALESCE((SELECT SUM(a.allocated_amount) FROM payment_allocations a JOIN payments p ON p.id=a.payment_id WHERE a.invoice_id=i.id AND p.status='APPLIED'),0)>=i.subtotal-COALESCE(i.returned_total,0) THEN 'PAID'
 WHEN EXISTS(SELECT 1 FROM payment_allocations a JOIN payments p ON p.id=a.payment_id WHERE a.invoice_id=i.id AND p.status='APPLIED') THEN 'PARTIALLY_PAID'
 ELSE i.status END;

-- The historical June register examples have no June business activity in this fixture.
-- Reset their summaries to the real (empty) period rather than inventing receipts.
DELETE FROM cash_register_details;
UPDATE cash_registers SET total_payments=0,total_advances=0,total_returns=0,total_credit_notes=0,total_debit_notes=0,total_invoices=0,payment_count=0,advance_count=0,return_count=0,invoice_count=0,expected_closing_balance=0,actual_closing_balance=CASE WHEN status='CLOSED' THEN 0 ELSE NULL END,difference=CASE WHEN status='CLOSED' THEN 0 ELSE NULL END;
