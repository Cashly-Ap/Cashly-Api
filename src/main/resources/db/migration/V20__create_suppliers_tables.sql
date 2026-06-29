CREATE TABLE suppliers (
    id BINARY(16) PRIMARY KEY,
    code BIGINT NOT NULL UNIQUE,
    legal_name VARCHAR(200) NOT NULL,
    alias VARCHAR(100) NOT NULL,
    type ENUM('NATURAL','JURIDICO') NOT NULL,
    rif VARCHAR(50) NOT NULL,
    notes TEXT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_suppliers_code ON suppliers (code);
CREATE INDEX idx_suppliers_rif ON suppliers (rif);
CREATE INDEX idx_suppliers_active ON suppliers (active);
CREATE INDEX idx_suppliers_type ON suppliers (type);

CREATE TABLE supplier_emails (
    id BINARY(16) PRIMARY KEY,
    supplier_id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    CONSTRAINT fk_supplier_emails_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE INDEX idx_supplier_emails_supplier_id ON supplier_emails (supplier_id);

CREATE TABLE supplier_phones (
    id BINARY(16) PRIMARY KEY,
    supplier_id BINARY(16) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    CONSTRAINT fk_supplier_phones_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE INDEX idx_supplier_phones_supplier_id ON supplier_phones (supplier_id);

CREATE TABLE supplier_addresses (
    id BINARY(16) PRIMARY KEY,
    supplier_id BINARY(16) NOT NULL,
    address TEXT NOT NULL,
    billing BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    state VARCHAR(100) NULL,
    municipality VARCHAR(100) NULL,
    postal_code VARCHAR(10) NULL,
    CONSTRAINT fk_supplier_addresses_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE
);

CREATE INDEX idx_supplier_addresses_supplier_id ON supplier_addresses (supplier_id);
CREATE INDEX idx_supplier_addresses_billing ON supplier_addresses (billing);
