CREATE TABLE `return_cancelled_allocations` (
  `id` binary(16) NOT NULL,
  `return_id` binary(16) NOT NULL,
  `payment_id` binary(16) NOT NULL,
  `invoice_id` binary(16) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  KEY `fk_return_cancelled_allocations_return` (`return_id`),
  KEY `fk_return_cancelled_allocations_company` (`company_id`),
  CONSTRAINT `fk_return_cancelled_allocations_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_return_cancelled_allocations_return` FOREIGN KEY (`return_id`) REFERENCES `returns` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
