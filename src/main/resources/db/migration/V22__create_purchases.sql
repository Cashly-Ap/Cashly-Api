CREATE TABLE `purchases` (
  `id` binary(16) NOT NULL,
  `purchase_number` varchar(20) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `supplier_name` varchar(200) DEFAULT NULL,
  `supplier_rif` varchar(50) DEFAULT NULL,
  `created_by` binary(16) DEFAULT NULL,
  `created_by_name` varchar(200) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `notes` varchar(1000) DEFAULT NULL,
  `subtotal` decimal(15,2) DEFAULT NULL,
  `confirmed_at` timestamp NULL DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `cancelled_reason` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `purchase_number` (`purchase_number`),
  KEY `idx_purchases_number` (`purchase_number`),
  KEY `idx_purchases_supplier` (`supplier_id`),
  KEY `idx_purchases_created_by` (`created_by`),
  KEY `idx_purchases_status` (`status`),
  KEY `idx_purchases_created_at` (`created_at`),
  KEY `fk_purchases_company` (`company_id`),
  CONSTRAINT `fk_purchases_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_purchases_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_lines` (
  `id` binary(16) NOT NULL,
  `purchase_id` binary(16) NOT NULL,
  `product_id` binary(16) NOT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_code` varchar(50) DEFAULT NULL,
  `quantity` decimal(15,4) NOT NULL,
  `unit_cost` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) DEFAULT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_purchase_lines_purchase` (`purchase_id`),
  KEY `idx_purchase_lines_product` (`product_id`),
  KEY `fk_purchase_lines_company` (`company_id`),
  CONSTRAINT `fk_purchase_lines_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_purchase_lines_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_purchase_lines_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_line_allocations` (
  `id` binary(16) NOT NULL,
  `purchase_line_id` binary(16) NOT NULL,
  `warehouse_id` binary(16) NOT NULL,
  `quantity` decimal(15,4) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_pla_line` (`purchase_line_id`),
  KEY `idx_pla_warehouse` (`warehouse_id`),
  KEY `fk_purchase_line_allocations_company` (`company_id`),
  CONSTRAINT `fk_pla_line` FOREIGN KEY (`purchase_line_id`) REFERENCES `purchase_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_pla_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouses` (`id`),
  CONSTRAINT `fk_purchase_line_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_purchase_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_purchase_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
