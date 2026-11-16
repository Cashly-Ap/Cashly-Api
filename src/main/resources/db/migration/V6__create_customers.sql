CREATE TABLE `customers` (
  `id` binary(16) NOT NULL,
  `code` bigint NOT NULL,
  `legal_name` varchar(200) NOT NULL,
  `type` varchar(50) NOT NULL,
  `rif` varchar(50) NOT NULL,
  `alias` varchar(100) NOT NULL,
  `credit_days` int NOT NULL DEFAULT '0',
  `credit_limit` decimal(15,2) NOT NULL DEFAULT '0.00',
  `notes` text,
  `price_type_id` binary(16) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  UNIQUE KEY `rif` (`rif`),
  KEY `idx_customers_code` (`code`),
  KEY `idx_customers_active` (`active`),
  KEY `idx_customers_type` (`type`),
  KEY `idx_customers_price_type` (`price_type_id`),
  KEY `fk_customers_company` (`company_id`),
  CONSTRAINT `fk_customers_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_customers_price_type` FOREIGN KEY (`price_type_id`) REFERENCES `price_types` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `customer_emails` (
  `id` binary(16) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `email` varchar(255) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_customer_emails_customer_id` (`customer_id`),
  KEY `fk_customer_emails_company` (`company_id`),
  CONSTRAINT `fk_customer_emails_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_customer_emails_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `customer_phones` (
  `id` binary(16) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `phone` varchar(50) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_customer_phones_customer_id` (`customer_id`),
  KEY `fk_customer_phones_company` (`company_id`),
  CONSTRAINT `fk_customer_phones_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_customer_phones_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `customer_addresses` (
  `id` binary(16) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `address` text NOT NULL,
  `billing` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `state` varchar(100) DEFAULT NULL,
  `municipality` varchar(100) DEFAULT NULL,
  `postal_code` varchar(10) DEFAULT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_customer_addresses_customer_id` (`customer_id`),
  KEY `idx_customer_addresses_billing` (`billing`),
  KEY `fk_customer_addresses_company` (`company_id`),
  CONSTRAINT `fk_customer_addresses_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_customer_addresses_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `customer_balances` (
  `id` binary(16) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `total_balance` decimal(15,2) NOT NULL DEFAULT '0.00',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `customer_id` (`customer_id`),
  KEY `fk_customer_balances_company` (`company_id`),
  CONSTRAINT `customer_balances_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `fk_customer_balances_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `balance_tx_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_balance_tx_sequence_company` (`company_id`),
  CONSTRAINT `fk_balance_tx_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `balance_transactions` (
  `id` binary(16) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `type` varchar(20) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `reference_type` varchar(20) NOT NULL,
  `reference_id` binary(16) DEFAULT NULL,
  `number` varchar(100) DEFAULT NULL,
  `reference_number` varchar(100) DEFAULT NULL,
  `created_by` binary(16) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_bt_customer` (`customer_id`),
  KEY `idx_bt_type` (`type`),
  KEY `idx_bt_reference` (`reference_type`,`reference_id`),
  KEY `idx_bt_number` (`number`),
  KEY `idx_bt_reference_number` (`reference_number`),
  KEY `fk_balance_transactions_company` (`company_id`),
  CONSTRAINT `balance_transactions_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `fk_balance_transactions_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
