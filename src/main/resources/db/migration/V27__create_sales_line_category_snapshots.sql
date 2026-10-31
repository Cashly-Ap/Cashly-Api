CREATE TABLE `order_line_category_snapshots` (
  `order_line_id` binary(16) NOT NULL,
  `category_id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`order_line_id`),
  KEY `idx_order_line_category_snapshot_category` (`category_id`),
  KEY `fk_order_line_category_snapshots_company` (`company_id`),
  CONSTRAINT `fk_order_line_category_snapshot_line` FOREIGN KEY (`order_line_id`) REFERENCES `order_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_line_category_snapshots_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `quote_line_category_snapshots` (
  `quote_line_id` binary(16) NOT NULL,
  `category_id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`quote_line_id`),
  KEY `idx_quote_line_category_snapshot_category` (`category_id`),
  KEY `fk_quote_line_category_snapshots_company` (`company_id`),
  CONSTRAINT `fk_quote_line_category_snapshot_line` FOREIGN KEY (`quote_line_id`) REFERENCES `quote_lines` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_quote_line_category_snapshots_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
