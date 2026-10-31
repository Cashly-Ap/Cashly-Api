CREATE TABLE `invoice_line_allocations` (
  `id` binary(16) NOT NULL,
  `invoice_line_id` binary(16) NOT NULL,
  `warehouse_id` binary(16) NOT NULL,
  `allocated` decimal(15,4) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `idx_ila_invoice_line_id` (`invoice_line_id`),
  KEY `fk_invoice_line_allocations_company` (`company_id`),
  CONSTRAINT `fk_ila_invoice_line` FOREIGN KEY (`invoice_line_id`) REFERENCES `invoice_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_invoice_line_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
