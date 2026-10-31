-- Reconcile warehouse_stock.reserved with the stock held by active documents.
--
-- Confirmed orders and invoices already deduct the allocated quantity from
-- warehouse_stock.quantity, so only documents still in DRAFT hold a live
-- reservation. Recomputing from those allocations repairs rows whose reserved
-- value drifted (seed data, cancelled confirmations, partially applied fixes).
UPDATE `warehouse_stock` AS ws
SET ws.`reserved` = COALESCE(
  (
    SELECT SUM(alloc.`allocated`)
    FROM (
      SELECT ola.`warehouse_id` AS `warehouse_id`,
             ol.`product_id`   AS `product_id`,
             ola.`allocated`   AS `allocated`
      FROM `order_line_allocations` ola
      JOIN `order_lines` ol ON ol.`id` = ola.`order_line_id`
      JOIN `orders` o ON o.`id` = ol.`order_id`
      WHERE o.`status` = 'DRAFT'
      UNION ALL
      SELECT ila.`warehouse_id`,
             il.`product_id`,
             ila.`allocated`
      FROM `invoice_line_allocations` ila
      JOIN `invoice_lines` il ON il.`id` = ila.`invoice_line_id`
      JOIN `invoices` i ON i.`id` = il.`invoice_id`
      WHERE i.`status` = 'DRAFT'
    ) AS alloc
    WHERE alloc.`warehouse_id` = ws.`warehouse_id`
      AND alloc.`product_id` = ws.`product_id`
  ),
  0
);
