CREATE TABLE `returns` (
  `id` binary(16) NOT NULL,
  `return_number` varchar(20) NOT NULL,
  `invoice_id` binary(16) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `return_type` varchar(20) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `total` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_by` binary(16) NOT NULL,
  `created_by_name` varchar(200) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `return_number` (`return_number`),
  KEY `idx_returns_return_number` (`return_number`),
  KEY `idx_returns_invoice_id` (`invoice_id`),
  KEY `idx_returns_customer_id` (`customer_id`),
  KEY `idx_returns_return_type` (`return_type`),
  KEY `idx_returns_created_at` (`created_at`),
  KEY `fk_returns_created_by` (`created_by`),
  KEY `fk_returns_company` (`company_id`),
  CONSTRAINT `fk_returns_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_returns_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_returns_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `fk_returns_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `return_lines` (
  `id` binary(16) NOT NULL,
  `return_id` binary(16) NOT NULL,
  `invoice_line_id` binary(16) NOT NULL,
  `product_id` binary(16) NOT NULL,
  `quantity` decimal(15,4) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_return_lines_return_id` (`return_id`),
  KEY `idx_return_lines_product_id` (`product_id`),
  KEY `fk_return_lines_invoice_line` (`invoice_line_id`),
  KEY `fk_return_lines_company` (`company_id`),
  CONSTRAINT `fk_return_lines_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_return_lines_invoice_line` FOREIGN KEY (`invoice_line_id`) REFERENCES `invoice_lines` (`id`),
  CONSTRAINT `fk_return_lines_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_return_lines_return` FOREIGN KEY (`return_id`) REFERENCES `returns` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `return_line_allocations` (
  `id` binary(16) NOT NULL,
  `return_line_id` binary(16) NOT NULL,
  `warehouse_id` binary(16) NOT NULL,
  `allocated` decimal(15,4) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `fk_rla_return_line` (`return_line_id`),
  KEY `fk_rla_warehouse` (`warehouse_id`),
  KEY `fk_return_line_allocations_company` (`company_id`),
  CONSTRAINT `fk_return_line_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_rla_return_line` FOREIGN KEY (`return_line_id`) REFERENCES `return_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rla_warehouse` FOREIGN KEY (`warehouse_id`) REFERENCES `warehouses` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `return_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_return_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_return_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
