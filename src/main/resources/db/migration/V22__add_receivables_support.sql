-- Add new payment statuses for approval workflow
ALTER TABLE payments
MODIFY COLUMN status ENUM(
    'DRAFT', 'APPLIED', 'PARTIALLY_APPLIED', 'CANCELLED',
    'PENDING_APPROVAL', 'APPROVED', 'REJECTED'
) NOT NULL DEFAULT 'DRAFT';

-- Add approval and voucher columns
ALTER TABLE payments
    ADD COLUMN voucher_path       VARCHAR(500)   NULL AFTER applied_amount,
    ADD COLUMN approval_status    VARCHAR(20)    NULL AFTER voucher_path,
    ADD COLUMN approved_by        BINARY(16)     NULL AFTER approval_status,
    ADD COLUMN approved_at        TIMESTAMP      NULL AFTER approved_by,
    ADD COLUMN rejection_reason   VARCHAR(500)   NULL AFTER approved_at,
    ADD COLUMN commission_percentage DECIMAL(5,2) NULL AFTER rejection_reason;

-- Index approval-related queries
CREATE INDEX idx_payments_approval_status ON payments(approval_status);
CREATE INDEX idx_payments_approved_by    ON payments(approved_by);
