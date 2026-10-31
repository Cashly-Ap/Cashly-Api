CREATE TABLE `orders` (
  `id` binary(16) NOT NULL,
  `order_number` varchar(20) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `customer_name` varchar(200) NOT NULL,
  `customer_rif` varchar(50) NOT NULL,
  `created_by` binary(16) DEFAULT NULL,
  `created_by_name` varchar(200) DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `delivery_date` date DEFAULT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `subtotal` decimal(15,2) NOT NULL DEFAULT '0.00',
  `invoice_id` binary(16) DEFAULT NULL,
  `invoice_number` varchar(20) DEFAULT NULL,
  `cancelled_at` timestamp NULL DEFAULT NULL,
  `cancelled_reason` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_number` (`order_number`),
  KEY `idx_orders_order_number` (`order_number`),
  KEY `idx_orders_customer_id` (`customer_id`),
  KEY `idx_orders_created_by` (`created_by`),
  KEY `idx_orders_status` (`status`),
  KEY `idx_orders_delivery_date` (`delivery_date`),
  KEY `idx_orders_created_at` (`created_at`),
  KEY `fk_orders_company` (`company_id`),
  CONSTRAINT `fk_orders_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_orders_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_lines` (
  `id` binary(16) NOT NULL,
  `order_id` binary(16) NOT NULL,
  `product_id` binary(16) NOT NULL,
  `product_name` varchar(200) NOT NULL,
  `product_code` varchar(50) NOT NULL,
  `quantity` decimal(15,4) NOT NULL,
  `unit_price` decimal(15,2) NOT NULL,
  `line_total` decimal(15,2) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_order_lines_order_id` (`order_id`),
  KEY `idx_order_lines_product_id` (`product_id`),
  KEY `fk_order_lines_company` (`company_id`),
  CONSTRAINT `fk_order_lines_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_order_lines_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `fk_order_lines_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_order_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_order_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
