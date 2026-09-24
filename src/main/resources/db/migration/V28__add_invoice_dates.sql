ALTER TABLE invoices
    ADD COLUMN issue_date DATE NULL AFTER updated_at,
    ADD COLUMN due_date DATE NULL AFTER issue_date;

UPDATE invoices i
JOIN customers c ON c.id = i.customer_id
SET i.issue_date = DATE(i.created_at),
    i.due_date = DATE_ADD(DATE(i.created_at), INTERVAL c.credit_days DAY);

ALTER TABLE invoices
    MODIFY COLUMN issue_date DATE NOT NULL,
    MODIFY COLUMN due_date DATE NOT NULL;
