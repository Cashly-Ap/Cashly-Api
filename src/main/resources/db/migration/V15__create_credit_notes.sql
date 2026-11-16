CREATE TABLE `credit_notes` (
  `id` binary(16) NOT NULL,
  `note_number` varchar(20) NOT NULL,
  `customer_id` binary(16) NOT NULL,
  `invoice_id` binary(16) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `reason` varchar(500) NOT NULL,
  `created_by` binary(16) NOT NULL,
  `created_by_name` varchar(200) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `note_number` (`note_number`),
  KEY `idx_credit_notes_note_number` (`note_number`),
  KEY `idx_credit_notes_customer_id` (`customer_id`),
  KEY `idx_credit_notes_invoice_id` (`invoice_id`),
  KEY `idx_credit_notes_created_at` (`created_at`),
  KEY `fk_credit_notes_created_by` (`created_by`),
  KEY `fk_credit_notes_company` (`company_id`),
  CONSTRAINT `fk_credit_notes_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_credit_notes_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_credit_notes_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`),
  CONSTRAINT `fk_credit_notes_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `credit_note_number_sequence` (
  `year` int NOT NULL,
  `next_value` int NOT NULL DEFAULT '1',
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`year`),
  KEY `fk_credit_note_number_sequence_company` (`company_id`),
  CONSTRAINT `fk_credit_note_number_sequence_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
