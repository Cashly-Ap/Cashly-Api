INSERT INTO order_line_category_snapshots (order_line_id, category_id)
SELECT ol.id, p.category_id
FROM order_lines ol
JOIN products p ON p.id = ol.product_id
WHERE p.category_id IS NOT NULL
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id);

INSERT INTO quote_line_category_snapshots (quote_line_id, category_id)
SELECT ql.id, p.category_id
FROM quote_lines ql
JOIN products p ON p.id = ql.product_id
WHERE p.category_id IS NOT NULL
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id);
