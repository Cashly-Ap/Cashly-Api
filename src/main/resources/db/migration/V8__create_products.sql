CREATE TABLE `products` (
  `id` binary(16) NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(200) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `category_id` binary(16) NOT NULL,
  `composite` tinyint(1) NOT NULL DEFAULT '0',
  `purchase_price` decimal(15,2) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `photo` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `idx_product_code` (`code`),
  KEY `idx_product_name` (`name`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_composite` (`composite`),
  KEY `idx_product_active` (`active`),
  KEY `fk_products_company` (`company_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `fk_products_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product_components` (
  `id` binary(16) NOT NULL,
  `parent_product_id` binary(16) NOT NULL,
  `component_product_id` binary(16) NOT NULL,
  `quantity` int NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_parent_component` (`parent_product_id`,`component_product_id`),
  KEY `idx_pc_parent` (`parent_product_id`),
  KEY `idx_pc_component` (`component_product_id`),
  KEY `fk_product_components_company` (`company_id`),
  CONSTRAINT `fk_pc_component` FOREIGN KEY (`component_product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_pc_parent` FOREIGN KEY (`parent_product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `fk_product_components_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `chk_pc_quantity` CHECK ((`quantity` >= 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `product_code_sequence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `fk_product_code_sequence_company` (`company_id`),
  CONSTRAINT `fk_product_code_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
