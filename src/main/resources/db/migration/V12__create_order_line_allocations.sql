CREATE TABLE `order_line_allocations` (
  `id` binary(16) NOT NULL,
  `order_line_id` binary(16) NOT NULL,
  `warehouse_id` binary(16) NOT NULL,
  `allocated` decimal(15,4) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `fk_ola_order_line` (`order_line_id`),
  KEY `fk_order_line_allocations_company` (`company_id`),
  CONSTRAINT `fk_ola_order_line` FOREIGN KEY (`order_line_id`) REFERENCES `order_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_line_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
