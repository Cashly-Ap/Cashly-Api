CREATE TABLE `payment_methods` (
  `id` binary(16) NOT NULL,
  `code` varchar(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `fk_payment_methods_company` (`company_id`),
  CONSTRAINT `fk_payment_methods_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payments` (
  `id` binary(16) NOT NULL,
  `payment_number` varchar(20) NOT NULL,
  `customer_id` binary(16) NOT NULL,
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
  KEY `idx_payments_payment_number` (`payment_number`),
  KEY `idx_payments_customer_id` (`customer_id`),
  KEY `idx_payments_status` (`status`),
  KEY `idx_payments_payment_date` (`payment_date`),
  KEY `idx_payments_created_at` (`created_at`),
  KEY `idx_payments_verified_by` (`verified_by`),
  KEY `fk_payments_payment_method` (`payment_method_id`),
  KEY `fk_payments_created_by` (`created_by`),
  KEY `fk_payments_company` (`company_id`),
  CONSTRAINT `fk_payments_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_payments_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_payments_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `fk_payments_payment_method` FOREIGN KEY (`payment_method_id`) REFERENCES `payment_methods` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_allocations` (
  `id` binary(16) NOT NULL,
  `payment_id` binary(16) NOT NULL,
  `invoice_id` binary(16) NOT NULL,
  `allocated_amount` decimal(15,2) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_payment_allocations_payment_id` (`payment_id`),
  KEY `idx_payment_allocations_invoice_id` (`invoice_id`),
  KEY `fk_payment_allocations_company` (`company_id`),
  CONSTRAINT `fk_payment_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_payment_allocations_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`),
  CONSTRAINT `fk_payment_allocations_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_payment_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_payment_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
