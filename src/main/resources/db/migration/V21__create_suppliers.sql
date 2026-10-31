CREATE TABLE `suppliers` (
  `id` binary(16) NOT NULL,
  `code` bigint NOT NULL,
  `legal_name` varchar(200) NOT NULL,
  `alias` varchar(100) NOT NULL,
  `type` varchar(50) NOT NULL,
  `rif` varchar(50) NOT NULL,
  `notes` text,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_suppliers_code` (`code`),
  KEY `idx_suppliers_rif` (`rif`),
  KEY `idx_suppliers_active` (`active`),
  KEY `idx_suppliers_type` (`type`),
  KEY `fk_suppliers_company` (`company_id`),
  CONSTRAINT `fk_suppliers_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `supplier_emails` (
  `id` binary(16) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `email` varchar(255) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_supplier_emails_supplier_id` (`supplier_id`),
  KEY `fk_supplier_emails_company` (`company_id`),
  CONSTRAINT `fk_supplier_emails_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_supplier_emails_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `supplier_phones` (
  `id` binary(16) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `phone` varchar(50) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_supplier_phones_supplier_id` (`supplier_id`),
  KEY `fk_supplier_phones_company` (`company_id`),
  CONSTRAINT `fk_supplier_phones_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_supplier_phones_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `supplier_addresses` (
  `id` binary(16) NOT NULL,
  `supplier_id` binary(16) NOT NULL,
  `address` text NOT NULL,
  `billing` tinyint(1) NOT NULL DEFAULT '0',
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `state` varchar(100) DEFAULT NULL,
  `municipality` varchar(100) DEFAULT NULL,
  `postal_code` varchar(10) DEFAULT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_supplier_addresses_supplier_id` (`supplier_id`),
  KEY `idx_supplier_addresses_billing` (`billing`),
  KEY `fk_supplier_addresses_company` (`company_id`),
  CONSTRAINT `fk_supplier_addresses_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_supplier_addresses_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
