-- V18: Expand movement types and add reference tracking for full traceability
-- This migration adds specific movement types (RETURN_ENTRY, CANCEL_ENTRY, SALE_EXIT)
-- and reference columns to track the source document of each movement.

-- Step 1: Expand movement_type ENUM with 3 new values
ALTER TABLE stock_movements
    MODIFY COLUMN movement_type ENUM(
        'ENTRY',
        'EXIT',
        'ADJUSTMENT',
        'TRANSFER_IN',
        'TRANSFER_OUT',
        'COMPONENT_EXIT',
        'RETURN_ENTRY',
        'CANCEL_ENTRY',
        'SALE_EXIT'
    ) NOT NULL,
    ALGORITHM=INPLACE, LOCK=NONE;

-- Step 2: Add reference tracking columns (all nullable for backward compatibility)
ALTER TABLE stock_movements
    ADD COLUMN reference_type ENUM('ORDER', 'INVOICE', 'RETURN', 'TRANSFER', 'MANUAL') NULL AFTER transfer_batch_id,
    ADD COLUMN reference_id BINARY(16) NULL AFTER reference_type,
    ADD COLUMN reference_number VARCHAR(100) NULL AFTER reference_id,
    ADD INDEX idx_sm_ref_type_id (reference_type, reference_id);

-- Notes:
-- - reference_type: Identifies the document type (ORDER, INVOICE, RETURN, TRANSFER, MANUAL)
-- - reference_id: UUID of the source document
-- - reference_number: Human-readable number (e.g., "RET-2025-001", "INV-2025-042", "ORD-2025-010")
-- - All columns are nullable to maintain backward compatibility with existing movements
-- - Index on (reference_type, reference_id) for efficient lookups
