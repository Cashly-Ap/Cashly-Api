-- Migration: Add verification tracking columns to payments
-- Supports admin approval/rejection flow for portal-registered payments

ALTER TABLE payments
    ADD COLUMN cancelled_reason VARCHAR(500) NULL AFTER updated_at,
    ADD COLUMN verified_by BINARY(16) NULL AFTER cancelled_reason,
    ADD COLUMN verified_by_name VARCHAR(200) NULL AFTER verified_by,
    ADD COLUMN verified_at TIMESTAMP NULL AFTER verified_by_name;

ALTER TABLE payments
    ADD INDEX idx_payments_verified_by (verified_by);
