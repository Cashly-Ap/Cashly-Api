-- V19: Create cash register tables for daily financial reconciliation
-- This enables the cash register workflow:
--   1. OPEN  - Start of day, operator opens a new register for today
--   2. CLOSED - End of day, totals are calculated, and register is closed

CREATE TABLE cash_registers (
    id BINARY(16) PRIMARY KEY,
    closure_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    opened_by BINARY(16) NOT NULL,
    opened_by_name VARCHAR(255) NOT NULL,
    opened_at DATETIME NOT NULL,
    opening_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    closed_by BINARY(16) NULL,
    closed_by_name VARCHAR(255) NULL,
    closed_at DATETIME NULL,
    total_payments DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_advances DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_returns DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_credit_notes DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_debit_notes DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_invoices DECIMAL(15,2) NOT NULL DEFAULT 0,
    payment_count INT NOT NULL DEFAULT 0,
    advance_count INT NOT NULL DEFAULT 0,
    return_count INT NOT NULL DEFAULT 0,
    invoice_count INT NOT NULL DEFAULT 0,
    expected_closing_balance DECIMAL(15,2) NOT NULL DEFAULT 0,
    actual_closing_balance DECIMAL(15,2) NULL,
    difference DECIMAL(15,2) NULL,
    total_income DECIMAL(15,2) NOT NULL DEFAULT 0,
    total_expense DECIMAL(15,2) NOT NULL DEFAULT 0,
    closing_notes TEXT NULL,
    version INT NOT NULL DEFAULT 0,
    open_slot TINYINT GENERATED ALWAYS AS (CASE WHEN status = 'OPEN' THEN 1 ELSE NULL END) STORED,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_register_opened_by FOREIGN KEY (opened_by) REFERENCES users(id),
    CONSTRAINT fk_register_closed_by FOREIGN KEY (closed_by) REFERENCES users(id),
    INDEX idx_closure_date (closure_date),
    INDEX idx_status (status),
    UNIQUE INDEX uq_cash_register_single_open (open_slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cash_register_details (
    id BINARY(16) PRIMARY KEY,
    closure_id BINARY(16) NOT NULL,
    payment_method_id BINARY(16) NULL,
    payment_method_name VARCHAR(255) NOT NULL,
    transaction_count INT NOT NULL DEFAULT 0,
    expected_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    actual_amount DECIMAL(15,2) NULL,
    difference DECIMAL(15,2) NULL,
    CONSTRAINT fk_detail_register FOREIGN KEY (closure_id) REFERENCES cash_registers(id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_methods(id),
    INDEX idx_detail_closure_id (closure_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
