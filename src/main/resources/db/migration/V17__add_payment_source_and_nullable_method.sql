-- Migration: Add payment source tracking and allow nullable payment method
-- This enables tracking payments made with customer balance (saldo a favor)

ALTER TABLE payments
    ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'PAYMENT_METHOD' AFTER payment_method_id;

ALTER TABLE payments
    MODIFY COLUMN payment_method_id BINARY(16) NULL;

ALTER TABLE payments
    DROP FOREIGN KEY fk_payments_payment_method;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_payment_method
        FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id)
        ON DELETE SET NULL;
