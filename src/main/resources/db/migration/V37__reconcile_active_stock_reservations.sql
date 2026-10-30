UPDATE warehouse_stock w
SET reserved = COALESCE((
        SELECT SUM(a.allocated)
        FROM order_line_allocations a
        JOIN order_lines l ON l.id = a.order_line_id
        JOIN orders o ON o.id = l.order_id
        WHERE a.warehouse_id = w.warehouse_id
          AND l.product_id = w.product_id
          AND o.status = 'DRAFT'
    ), 0) + COALESCE((
        SELECT SUM(a.allocated)
        FROM invoice_line_allocations a
        JOIN invoice_lines l ON l.id = a.invoice_line_id
        JOIN invoices i ON i.id = l.invoice_id
        WHERE a.warehouse_id = w.warehouse_id
          AND l.product_id = w.product_id
          AND i.status = 'DRAFT'
    ), 0),
    updated_at = CURRENT_TIMESTAMP(6);
