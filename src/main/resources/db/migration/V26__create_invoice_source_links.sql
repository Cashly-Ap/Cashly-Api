CREATE TABLE `invoice_order_links` (
  `invoice_id` binary(16) NOT NULL,
  `order_id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`invoice_id`,`order_id`),
  KEY `fk_invoice_order_links_order` (`order_id`),
  KEY `fk_invoice_order_links_company` (`company_id`),
  CONSTRAINT `fk_invoice_order_links_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_invoice_order_links_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_invoice_order_links_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `invoice_quote_links` (
  `invoice_id` binary(16) NOT NULL,
  `quote_id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`invoice_id`,`quote_id`),
  KEY `fk_invoice_quote_links_quote` (`quote_id`),
  KEY `fk_invoice_quote_links_company` (`company_id`),
  CONSTRAINT `fk_invoice_quote_links_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_invoice_quote_links_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_invoice_quote_links_quote` FOREIGN KEY (`quote_id`) REFERENCES `quotes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
