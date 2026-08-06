-- =============================================================================
-- V25: Make created_by nullable on quotes, orders and payments
-- Portal-created documents have no system user (createdBy = NULL).
-- =============================================================================

ALTER TABLE quotes
    MODIFY COLUMN created_by BINARY(16) NULL,
    MODIFY COLUMN created_by_name VARCHAR(200) NULL;

ALTER TABLE orders
    MODIFY COLUMN created_by BINARY(16) NULL,
    MODIFY COLUMN created_by_name VARCHAR(200) NULL;

ALTER TABLE payments
    MODIFY COLUMN created_by BINARY(16) NULL,
    MODIFY COLUMN created_by_name VARCHAR(200) NULL;
