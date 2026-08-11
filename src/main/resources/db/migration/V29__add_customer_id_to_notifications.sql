ALTER TABLE notifications
    ADD COLUMN customer_id BINARY(16) NULL AFTER user_id,
    MODIFY COLUMN user_id BINARY(16) NULL,
    ADD INDEX idx_notifications_customer_id (customer_id);
