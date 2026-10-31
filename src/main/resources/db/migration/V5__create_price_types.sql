CREATE TABLE `price_types` (
  `id` binary(16) NOT NULL,
  `name` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_price_type_name` (`name`),
  KEY `fk_price_types_company` (`company_id`),
  CONSTRAINT `fk_price_types_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
