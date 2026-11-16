CREATE TABLE `product_prices` (
  `id` binary(16) NOT NULL,
  `product_id` binary(16) NOT NULL,
  `price_type_id` binary(16) NOT NULL,
  `price` decimal(19,4) NOT NULL,
  `profit_margin` decimal(5,2) DEFAULT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_product_price_type` (`product_id`,`price_type_id`),
  KEY `idx_product_prices_product_id` (`product_id`),
  KEY `idx_product_prices_price_type_id` (`price_type_id`),
  KEY `fk_product_prices_company` (`company_id`),
  CONSTRAINT `fk_product_prices_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_product_prices_price_type` FOREIGN KEY (`price_type_id`) REFERENCES `price_types` (`id`),
  CONSTRAINT `fk_product_prices_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
