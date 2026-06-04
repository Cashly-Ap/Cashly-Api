CREATE TABLE customers (
    id BINARY(16) PRIMARY KEY,
    code BIGINT NOT NULL UNIQUE,
    legal_name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    rif VARCHAR(50) NOT NULL,
    alias VARCHAR(100) NOT NULL,
    credit_days INT NOT NULL DEFAULT 0,
    credit_limit DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    notes TEXT NULL,
    price_type_id BINARY(16) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    portal_access BOOLEAN NOT NULL DEFAULT FALSE,
    portal_password VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_customers_code ON customers (code);
CREATE INDEX idx_customers_rif ON customers (rif);
CREATE INDEX idx_customers_active ON customers (active);
CREATE INDEX idx_customers_type ON customers (type);
CREATE INDEX idx_customers_price_type ON customers (price_type_id);

CREATE TABLE customer_emails (
    id BINARY(16) PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT fk_customer_emails_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_emails_customer_id ON customer_emails (customer_id);

CREATE TABLE customer_phones (
    id BINARY(16) PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    CONSTRAINT fk_customer_phones_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_phones_customer_id ON customer_phones (customer_id);

CREATE TABLE customer_addresses (
    id BINARY(16) PRIMARY KEY,
    customer_id BINARY(16) NOT NULL,
    address TEXT NOT NULL,
    billing BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    state VARCHAR(100) NULL,
    municipality VARCHAR(100) NULL,
    postal_code VARCHAR(10) NULL,
    CONSTRAINT fk_customer_addresses_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE INDEX idx_customer_addresses_customer_id ON customer_addresses (customer_id);
CREATE INDEX idx_customer_addresses_billing ON customer_addresses (billing);

CREATE TABLE customer_balances (
    id            BINARY(16) PRIMARY KEY,
    customer_id   BINARY(16) NOT NULL UNIQUE,
    total_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE TABLE balance_transactions (
    id              BINARY(16) PRIMARY KEY,
    customer_id     BINARY(16) NOT NULL,
    type            ENUM('ADVANCED','RETURNS','PAYMENTS','INVOICES','REFUND') NOT NULL,
    amount          DECIMAL(15,2) NOT NULL,
    reference_type  ENUM('ADVANCED','RETURNS','PAYMENTS','INVOICES','REFUND') NOT NULL,
    reference_id    BINARY(16),
    notes           VARCHAR(500),
    created_by      BINARY(16),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

CREATE INDEX idx_bt_customer ON balance_transactions(customer_id);
CREATE INDEX idx_bt_type ON balance_transactions(type);
CREATE INDEX idx_bt_reference ON balance_transactions(reference_type, reference_id);

CREATE TABLE balance_tx_sequence (
    year       INT PRIMARY KEY,
    next_value INT NOT NULL DEFAULT 1
);
