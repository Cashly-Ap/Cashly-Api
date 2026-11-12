CREATE TABLE `auth_user_preferences` (
  `user_id` binary(16) NOT NULL,
  `company_id` binary(16) NOT NULL DEFAULT 0x92000000000000000000000000000001,
  `theme_mode` varchar(10) NOT NULL DEFAULT 'system',
  `color_mode` varchar(15) NOT NULL DEFAULT 'minimal',
  `shortcuts_enabled` tinyint(1) NOT NULL DEFAULT '0',
  `show_shortcut_hints` tinyint(1) NOT NULL DEFAULT '1',
  `language` varchar(2) NOT NULL DEFAULT 'es',
  PRIMARY KEY (`user_id`),
  KEY `fk_auth_user_preferences_company` (`company_id`),
  CONSTRAINT `fk_auth_user_preferences_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_auth_user_preferences_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_auth_user_preferences_colors` CHECK ((`color_mode` in (_utf8mb4'minimal',_utf8mb4'informative'))),
  CONSTRAINT `chk_auth_user_preferences_language` CHECK ((`language` in (_utf8mb4'es',_utf8mb4'en'))),
  CONSTRAINT `chk_auth_user_preferences_theme` CHECK ((`theme_mode` in (_utf8mb4'system',_utf8mb4'light',_utf8mb4'dark')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
