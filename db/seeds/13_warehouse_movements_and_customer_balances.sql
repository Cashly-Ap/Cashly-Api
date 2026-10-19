-- Deterministic stock opening for every active warehouse/simple product pair.
-- Business movements are derived from their actual documents after purchases load.
INSERT INTO warehouse_stock (id,warehouse_id,product_id,quantity,reserved,last_movement_at)
SELECT UNHEX(MD5(CONCAT('stock:',HEX(w.id),HEX(p.id)))),w.id,p.id,10000,0,'2025-01-01 00:00:00'
FROM warehouses w CROSS JOIN products p WHERE w.active = TRUE AND p.composite = FALSE;
INSERT INTO stock_movements (id,warehouse_id,product_id,movement_type,quantity,previous_stock,new_stock,reference_type,notes,created_at)
SELECT UNHEX(MD5(CONCAT('opening:',HEX(id)))),warehouse_id,product_id,'ENTRY',quantity,0,quantity,'MANUAL','Beta opening balance','2025-01-01 00:00:00'
FROM warehouse_stock;
