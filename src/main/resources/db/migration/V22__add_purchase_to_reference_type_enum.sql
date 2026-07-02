-- V22: Add PURCHASE to reference_type ENUM for purchase stock movements
-- The Java ReferenceType enum already includes PURCHASE but the MySQL ENUM was missing it,
-- causing "Data truncated for column 'reference_type'" when confirming purchases.

ALTER TABLE stock_movements
    MODIFY COLUMN reference_type ENUM(
        'ORDER',
        'INVOICE',
        'RETURN',
        'TRANSFER',
        'PURCHASE',
        'MANUAL'
    ) NULL,
    ALGORITHM=COPY;
