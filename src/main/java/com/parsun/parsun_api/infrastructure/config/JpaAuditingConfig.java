package com.parsun.parsun_api.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import jakarta.persistence.EntityManager;

@Configuration
@EnableJpaAuditing
@ConditionalOnClass(EntityManager.class)
public class JpaAuditingConfig {
}
