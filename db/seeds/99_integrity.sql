-- Fail the mysql client (never invoke it with --force) if any full-data invariant fails.
DROP PROCEDURE IF EXISTS cashly_seed_assert;
DROP PROCEDURE IF EXISTS cashly_seed_check_foreign_keys;
DELIMITER $$
CREATE PROCEDURE cashly_seed_assert(IN violations BIGINT,IN description VARCHAR(128))
BEGIN
 IF violations<>0 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT=description; END IF;
END$$
CREATE PROCEDURE cashly_seed_check_foreign_keys()
BEGIN
 DECLARE finished BOOLEAN DEFAULT FALSE;
 DECLARE child_table_name,child_column_name,parent_table_name,parent_column_name VARCHAR(64);
 DECLARE refs CURSOR FOR SELECT TABLE_NAME,COLUMN_NAME,REFERENCED_TABLE_NAME,REFERENCED_COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA=DATABASE() AND REFERENCED_TABLE_NAME IS NOT NULL;
 DECLARE CONTINUE HANDLER FOR NOT FOUND SET finished=TRUE;
 OPEN refs;
 check_refs: LOOP
  FETCH refs INTO child_table_name,child_column_name,parent_table_name,parent_column_name;
  IF finished THEN LEAVE check_refs; END IF;
  SET @orphan_sql=CONCAT('SELECT COUNT(*) INTO @seed_orphans FROM `',child_table_name,'` c LEFT JOIN `',parent_table_name,'` p ON c.`',child_column_name,'`=p.`',parent_column_name,'` WHERE c.`',child_column_name,'` IS NOT NULL AND p.`',parent_column_name,'` IS NULL');
  PREPARE orphan_check FROM @orphan_sql; EXECUTE orphan_check; DEALLOCATE PREPARE orphan_check;
  CALL cashly_seed_assert(@seed_orphans,CONCAT('Orphan reference: ',child_table_name,'.',child_column_name));
 END LOOP;
 CLOSE refs;
END$$
DELIMITER ;
CALL cashly_seed_check_foreign_keys();
-- Mirror the API PermissionRegistry supported cells; omitted/unknown cells fail closed.
SET @seed_violations = (SELECT COUNT(*) FROM auth_role_permissions WHERE NOT (
 (module IN ('USERS','ROLES','DEPARTMENTS') AND action IN ('READ','CREATE','UPDATE','DELETE'))
 OR (module='CATALOG' AND action IN ('READ','CREATE','UPDATE','DELETE','EXPORT'))
 OR (module IN ('INVENTORY','SALES','FINANCES','PURCHASES') AND action IN ('READ','CREATE','UPDATE','DELETE','EXPORT','EXECUTE'))
 OR (module='DOCUMENT_ADMIN' AND action IN ('READ','EXPORT'))
 OR (module='DASHBOARD' AND action='READ')));
CALL cashly_seed_assert(@seed_violations,'Unsupported module/action grant (PermissionRegistry mismatch)');

SET @seed_violations = (SELECT COUNT(*) FROM users u JOIN auth_roles r ON r.id=u.role_id JOIN auth_departments d ON d.id=u.department_id WHERE TRIM(u.first_name)='' OR TRIM(u.last_name)='' OR NOT r.active OR NOT d.active);
CALL cashly_seed_assert(@seed_violations,'Incomplete user profiles/references');
SET @seed_violations = (SELECT COUNT(*) FROM auth_role_permissions p LEFT JOIN auth_role_permissions r ON r.role_id=p.role_id AND r.module=p.module AND r.action='READ' WHERE r.role_id IS NULL);
CALL cashly_seed_assert(@seed_violations,'Action missing module READ prerequisite');
SET @seed_violations = (SELECT COUNT(*) FROM auth_role_permissions WHERE role_id=UNHEX('90000000000000000000000000000006') AND (module='INVENTORY' OR module='CATALOG' AND action<>'READ'));
CALL cashly_seed_assert(@seed_violations,'Demo permissions differ from example matrix');
SET @seed_violations = (SELECT COUNT(*) FROM orders h WHERE ABS(h.subtotal-(SELECT COALESCE(SUM(l.line_total),0) FROM order_lines l WHERE l.order_id=h.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Order header/lines mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM quotes h WHERE ABS(h.subtotal-(SELECT COALESCE(SUM(l.line_total),0) FROM quote_lines l WHERE l.quote_id=h.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Quote header/lines mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM invoices h WHERE ABS(h.subtotal-(SELECT COALESCE(SUM(l.line_total),0) FROM invoice_lines l WHERE l.invoice_id=h.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Invoice header/lines mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM purchases h WHERE ABS(h.subtotal-(SELECT COALESCE(SUM(l.line_total),0) FROM purchase_lines l WHERE l.purchase_id=h.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Purchase header/lines mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM returns h WHERE ABS(h.total-(SELECT COALESCE(SUM(l.line_total),0) FROM return_lines l WHERE l.return_id=h.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Return header/lines mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM return_lines r JOIN invoice_lines l ON l.id=r.invoice_line_id JOIN returns h ON h.id=r.return_id JOIN invoices i ON i.id=l.invoice_id WHERE h.invoice_id<>i.id OR h.customer_id<>i.customer_id OR r.product_id<>l.product_id OR r.unit_price<>l.unit_price OR r.quantity<=0 OR ABS(r.quantity*r.unit_price-r.line_total)>0.01);
CALL cashly_seed_assert(@seed_violations,'Return source/price mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM invoice_lines l WHERE l.returned_quantity>l.quantity OR ABS(COALESCE(l.returned_quantity,0)-(SELECT COALESCE(SUM(r.quantity),0) FROM return_lines r WHERE r.invoice_line_id=l.id))>0.0001);
CALL cashly_seed_assert(@seed_violations,'Invoice returned quantities mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM payments p WHERE p.applied_amount<0 OR p.applied_amount>p.amount OR ABS(p.applied_amount-(SELECT COALESCE(SUM(a.allocated_amount),0) FROM payment_allocations a WHERE a.payment_id=p.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Payment allocation mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM payment_allocations a JOIN payments p ON p.id=a.payment_id JOIN invoices i ON i.id=a.invoice_id WHERE p.customer_id<>i.customer_id OR p.status<>'APPLIED' OR a.allocated_amount<=0);
CALL cashly_seed_assert(@seed_violations,'Payment allocation customer/status mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM customer_balances b WHERE ABS(b.total_balance-(SELECT COALESCE(SUM(t.amount),0) FROM balance_transactions t WHERE t.customer_id=b.customer_id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Customer balance ledger mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM warehouse_stock w WHERE w.quantity<0 OR w.reserved<0 OR w.reserved>w.quantity OR ABS(w.quantity-(SELECT COALESCE(SUM(CASE WHEN m.movement_type IN ('SALE_EXIT','EXIT','TRANSFER_OUT') THEN -m.quantity ELSE m.quantity END),0) FROM stock_movements m WHERE m.warehouse_id=w.warehouse_id AND m.product_id=w.product_id))>0.0001);
CALL cashly_seed_assert(@seed_violations,'Warehouse stock ledger mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM stock_movements m WHERE m.previous_stock<0 OR m.new_stock<0 OR ABS(m.new_stock-m.previous_stock-CASE WHEN m.movement_type IN ('SALE_EXIT','EXIT','TRANSFER_OUT') THEN -m.quantity ELSE m.quantity END)>0.0001);
CALL cashly_seed_assert(@seed_violations,'Stock movement arithmetic mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM cash_registers WHERE ABS(expected_closing_balance-(total_payments+total_advances+total_debit_notes-total_returns-total_credit_notes))>0.01);
CALL cashly_seed_assert(@seed_violations,'Cash register totals mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM order_lines WHERE ABS(quantity*unit_price-line_total)>0.01)+(SELECT COUNT(*) FROM invoice_lines WHERE ABS(quantity*unit_price-line_total)>0.01)+(SELECT COUNT(*) FROM quote_lines WHERE ABS(quantity*unit_price-line_total)>0.01)+(SELECT COUNT(*) FROM purchase_lines WHERE ABS(quantity*unit_cost-line_total)>0.01);
CALL cashly_seed_assert(@seed_violations,'Document line quantity/price arithmetic mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM invoice_lines l WHERE ABS(l.quantity-(SELECT COALESCE(SUM(a.allocated),0) FROM invoice_line_allocations a WHERE a.invoice_line_id=l.id))>0.0001);
CALL cashly_seed_assert(@seed_violations,'Invoice stock allocations mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM return_lines l WHERE ABS(l.quantity-(SELECT COALESCE(SUM(a.allocated),0) FROM return_line_allocations a WHERE a.return_line_id=l.id))>0.0001);
CALL cashly_seed_assert(@seed_violations,'Return stock allocations mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM credit_notes n JOIN invoices i ON i.id=n.invoice_id WHERE n.customer_id<>i.customer_id);
CALL cashly_seed_assert(@seed_violations,'Credit note customer mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM debit_notes n JOIN invoices i ON i.id=n.invoice_id WHERE n.customer_id<>i.customer_id);
CALL cashly_seed_assert(@seed_violations,'Debit note customer mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM stock_movements m LEFT JOIN warehouse_stock w ON w.warehouse_id=m.warehouse_id AND w.product_id=m.product_id WHERE w.id IS NULL);
CALL cashly_seed_assert(@seed_violations,'Movement missing warehouse stock pair');
SET @seed_violations = (SELECT COUNT(*) FROM invoices i LEFT JOIN users u ON u.id=i.created_by WHERE u.id IS NULL);
CALL cashly_seed_assert(@seed_violations,'Invoice creator missing');
SET @seed_violations = (SELECT COUNT(*) FROM invoices i WHERE ABS(i.returned_total-(SELECT COALESCE(SUM(r.total),0) FROM returns r WHERE r.invoice_id=i.id))>0.01);
CALL cashly_seed_assert(@seed_violations,'Invoice returned total mismatch');
SET @seed_violations = (SELECT COUNT(*) FROM stock_movements m WHERE m.reference_type='INVOICE' AND NOT EXISTS(SELECT 1 FROM invoices i WHERE i.id=m.reference_id) OR m.reference_type='RETURN' AND NOT EXISTS(SELECT 1 FROM returns r WHERE r.id=m.reference_id) OR m.reference_type='PURCHASE' AND NOT EXISTS(SELECT 1 FROM purchases p WHERE p.id=m.reference_id));
CALL cashly_seed_assert(@seed_violations,'Stock document reference missing');
SET @seed_violations = (SELECT COUNT(*) FROM payments p WHERE p.status='APPLIED' AND ABS(p.amount-p.applied_amount-(SELECT COALESCE(SUM(b.amount),0) FROM balance_transactions b WHERE b.reference_id=p.id AND b.type='ADVANCED'))>0.01);
CALL cashly_seed_assert(@seed_violations,'Advance credit differs from unapplied payment');
SET @seed_violations = (SELECT COUNT(*) FROM returns r WHERE ABS(r.total-(SELECT COALESCE(SUM(b.amount),0) FROM balance_transactions b WHERE b.reference_id=r.id AND b.type='RETURNS'))>0.01);
CALL cashly_seed_assert(@seed_violations,'Return credit differs from return total');
SET @seed_violations = IF((SELECT COUNT(*) FROM invoice_number_sequence WHERE year=2026 AND next_value=33)=1 AND (SELECT COUNT(*) FROM quote_number_sequence WHERE year=2026 AND next_value=15)=1,0,1);
CALL cashly_seed_assert(@seed_violations,'Document number sequences missing or stale');
DROP PROCEDURE cashly_seed_check_foreign_keys;
DROP PROCEDURE cashly_seed_assert;
SELECT 'Cashly beta seed integrity passed' AS result;
