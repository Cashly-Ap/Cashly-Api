-- Purchases: purchases, lines, allocations, supplier payments, returns and notes.

CREATE TABLE `purchases` (
  `id` binary(16) NOT NULL,
  `purchase_number` varchar(20) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `supplier_name` varchar(200) DEFAULT NULL,
  `supplier_rif` varchar(50) DEFAULT NULL,
  `created_by` binary(16) DEFAULT NULL,
  `created_by_name` varchar(200) DEFAULT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'DRAFT',
  `payment_type` varchar(20) DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `subtotal` decimal(15,2) NOT NULL DEFAULT '0.00',
  `returned_total` decimal(15,2) NOT NULL DEFAULT '0.00',
  `return_status` varchar(20) DEFAULT NULL,
  `issue_date` date NOT NULL,
  `due_date` date NOT NULL,
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
CREATE TABLE `supplier_payments` (
  `id` binary(16) NOT NULL,
  `payment_number` varchar(20) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `payment_method_id` binary(16) DEFAULT NULL,
  `source` varchar(20) NOT NULL DEFAULT 'PAYMENT_METHOD',
  `reference_number` varchar(100) DEFAULT NULL,
  `payment_date` date NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `applied_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_by` binary(16) DEFAULT NULL,
  `created_by_name` varchar(200) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cancelled_reason` varchar(500) DEFAULT NULL,
  `verified_by` binary(16) DEFAULT NULL,
  `verified_by_name` varchar(200) DEFAULT NULL,
  `verified_at` timestamp NULL DEFAULT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `payment_number` (`payment_number`),
  KEY `idx_supplier_payments_payment_number` (`payment_number`),
  KEY `idx_supplier_payments_supplier_id` (`supplier_id`),
  KEY `idx_supplier_payments_status` (`status`),
  KEY `idx_supplier_payments_payment_date` (`payment_date`),
  KEY `idx_supplier_payments_created_at` (`created_at`),
  KEY `fk_supplier_payments_payment_method` (`payment_method_id`),
  KEY `fk_supplier_payments_created_by` (`created_by`),
  KEY `fk_supplier_payments_company` (`company_id`),
  CONSTRAINT `fk_supplier_payments_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_supplier_payments_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_supplier_payments_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `fk_supplier_payments_payment_method` FOREIGN KEY (`payment_method_id`) REFERENCES `payment_methods` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `supplier_payment_allocations` (
  `id` binary(16) NOT NULL,
  `payment_id` binary(16) NOT NULL,
  `purchase_id` binary(16) NOT NULL,
  `allocated_amount` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_supplier_payment_allocations_payment_id` (`payment_id`),
  KEY `idx_supplier_payment_allocations_purchase_id` (`purchase_id`),
  KEY `fk_supplier_payment_allocations_company` (`company_id`),
  CONSTRAINT `fk_supplier_payment_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_supplier_payment_allocations_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`),
  CONSTRAINT `fk_supplier_payment_allocations_payment` FOREIGN KEY (`payment_id`) REFERENCES `supplier_payments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `supplier_payment_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_supplier_payment_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_supplier_payment_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `purchase_returns` (
  `id` binary(16) NOT NULL,
  `return_number` varchar(20) NOT NULL,
  `purchase_id` binary(16) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
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
  KEY `idx_purchase_returns_return_number` (`return_number`),
  KEY `idx_purchase_returns_purchase_id` (`purchase_id`),
  KEY `idx_purchase_returns_supplier_id` (`supplier_id`),
  KEY `idx_purchase_returns_return_type` (`return_type`),
  KEY `idx_purchase_returns_created_at` (`created_at`),
  KEY `fk_purchase_returns_created_by` (`created_by`),
  KEY `fk_purchase_returns_company` (`company_id`),
  CONSTRAINT `fk_purchase_returns_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_purchase_returns_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_purchase_returns_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `fk_purchase_returns_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_return_lines` (
  `id` binary(16) NOT NULL,
  `return_id` binary(16) NOT NULL,
  `purchase_line_id` binary(16) NOT NULL,
  `product_id` binary(16) NOT NULL,
  `quantity` decimal(15,4) NOT NULL,
  `unit_cost` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_purchase_return_lines_return_id` (`return_id`),
  KEY `idx_purchase_return_lines_product_id` (`product_id`),
  KEY `fk_purchase_return_lines_purchase_line` (`purchase_line_id`),
  KEY `fk_purchase_return_lines_company` (`company_id`),
  CONSTRAINT `fk_purchase_return_lines_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_purchase_return_lines_purchase_line` FOREIGN KEY (`purchase_line_id`) REFERENCES `purchase_lines` (`id`),
  CONSTRAINT `fk_purchase_return_lines_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_purchase_return_lines_return` FOREIGN KEY (`return_id`) REFERENCES `purchase_returns` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_return_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_purchase_return_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_purchase_return_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `purchase_credit_notes` (
  `id` binary(16) NOT NULL,
  `note_number` varchar(20) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `purchase_id` binary(16) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `created_by` binary(16) NOT NULL,
  `created_by_name` varchar(200) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `note_number` (`note_number`),
  KEY `idx_purchase_credit_notes_note_number` (`note_number`),
  KEY `idx_purchase_credit_notes_supplier_id` (`supplier_id`),
  KEY `idx_purchase_credit_notes_purchase_id` (`purchase_id`),
  KEY `idx_purchase_credit_notes_created_at` (`created_at`),
  KEY `fk_purchase_credit_notes_created_by` (`created_by`),
  KEY `fk_purchase_credit_notes_company` (`company_id`),
  CONSTRAINT `fk_purchase_credit_notes_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_purchase_credit_notes_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_purchase_credit_notes_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `fk_purchase_credit_notes_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_credit_note_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_purchase_credit_note_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_purchase_credit_note_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE `purchase_debit_notes` (
  `id` binary(16) NOT NULL,
  `note_number` varchar(20) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `purchase_id` binary(16) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `created_by` binary(16) NOT NULL,
  `created_by_name` varchar(200) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `note_number` (`note_number`),
  KEY `idx_purchase_debit_notes_note_number` (`note_number`),
  KEY `idx_purchase_debit_notes_supplier_id` (`supplier_id`),
  KEY `idx_purchase_debit_notes_purchase_id` (`purchase_id`),
  KEY `idx_purchase_debit_notes_created_at` (`created_at`),
  KEY `fk_purchase_debit_notes_created_by` (`created_by`),
  KEY `fk_purchase_debit_notes_company` (`company_id`),
  CONSTRAINT `fk_purchase_debit_notes_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_purchase_debit_notes_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_purchase_debit_notes_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`),
  CONSTRAINT `fk_purchase_debit_notes_purchase` FOREIGN KEY (`purchase_id`) REFERENCES `purchases` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `purchase_debit_note_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_purchase_debit_note_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_purchase_debit_note_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
