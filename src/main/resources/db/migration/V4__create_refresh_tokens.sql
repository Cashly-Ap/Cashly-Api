CREATE TABLE `refresh_tokens` (
  `id` binary(16) NOT NULL,
  `subject_id` binary(16) NOT NULL,
  `subject_type` varchar(20) NOT NULL DEFAULT 'USER',
  `token` varchar(500) NOT NULL,
  `expires_at` timestamp NOT NULL,
  `revoked` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `idx_refresh_tokens_subject_id` (`subject_id`),
  KEY `idx_refresh_tokens_subject_type` (`subject_type`),
  KEY `idx_refresh_tokens_token` (`token`),
  KEY `idx_refresh_tokens_expires_at` (`expires_at`),
  KEY `idx_refresh_tokens_revoked` (`revoked`),
  KEY `fk_refresh_tokens_company` (`company_id`),
  CONSTRAINT `fk_refresh_tokens_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
