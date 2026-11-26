CREATE INDEX `idx_invoices_customer_status_created`
    ON `invoices` (`customer_id`, `status`, `created_at`);

CREATE INDEX `idx_invoices_status_created`
    ON `invoices` (`status`, `created_at`);

CREATE INDEX `idx_invoices_status_due`
    ON `invoices` (`status`, `due_date`);

CREATE INDEX `idx_payments_customer_date`
    ON `payments` (`customer_id`, `payment_date`);
