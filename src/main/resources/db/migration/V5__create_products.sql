CREATE TABLE products (
    id             BINARY(16) PRIMARY KEY,
    code           VARCHAR(50) NOT NULL UNIQUE,
    name           VARCHAR(200) NOT NULL,
    description    VARCHAR(500),
    category_id    BINARY(16) NOT NULL,
    composite      BOOLEAN NOT NULL DEFAULT FALSE,
    unit           ENUM('UNIT','KG','LITER','METER','BOX','PACK') NOT NULL,
    base_price     DECIMAL(15, 2) NULL,
    purchase_price DECIMAL(15, 2) NULL,
    profit_margin  DECIMAL(5, 2)  NULL,
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_code      (code),
    INDEX idx_product_name      (name),
    INDEX idx_product_category  (category_id),
    INDEX idx_product_composite (composite),
    INDEX idx_product_active    (active),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE product_components (
    id                   BINARY(16) PRIMARY KEY,
    parent_product_id    BINARY(16) NOT NULL,
    component_product_id BINARY(16) NOT NULL,
    quantity             INT NOT NULL,
    unit                 ENUM('UNIT','KG','LITER','METER','BOX','PACK') NOT NULL DEFAULT 'UNIT',
    UNIQUE KEY uk_parent_component (parent_product_id, component_product_id),
    INDEX idx_pc_parent    (parent_product_id),
    INDEX idx_pc_component (component_product_id),
    CONSTRAINT chk_pc_quantity CHECK (quantity >= 1),
    CONSTRAINT fk_pc_parent    FOREIGN KEY (parent_product_id)    REFERENCES products(id),
    CONSTRAINT fk_pc_component FOREIGN KEY (component_product_id) REFERENCES products(id)
);
