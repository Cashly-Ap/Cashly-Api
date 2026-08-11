-- Migration: Add PENDING and REJECTED values to payment status ENUM
-- Needed for portal payment registration and admin verification flow

ALTER TABLE payments
    MODIFY COLUMN status ENUM('PENDING', 'APPLIED', 'CANCELLED', 'REJECTED', 'REFUND') NOT NULL DEFAULT 'PENDING';
