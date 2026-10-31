CREATE TABLE `auth_mfa` (
  `subject_id` varchar(36) NOT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT '0',
  `secret` varchar(255) DEFAULT NULL,
  `setup_expires_at` timestamp(6) NULL DEFAULT NULL,
  `last_step` bigint NOT NULL DEFAULT '-1',
  `recovery_hashes` text,
  `challenge_hash` varchar(64) DEFAULT NULL,
  `challenge_expires_at` timestamp(6) NULL DEFAULT NULL,
  `failures` int NOT NULL DEFAULT '0',
  `locked_until` timestamp(6) NULL DEFAULT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  PRIMARY KEY (`subject_id`),
  UNIQUE KEY `challenge_hash` (`challenge_hash`),
  KEY `fk_auth_mfa_company` (`company_id`),
  CONSTRAINT `fk_auth_mfa_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
