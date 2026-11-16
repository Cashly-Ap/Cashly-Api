package com.cashly.cashly_api.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan(basePackages = "com.cashly.cashly_api")
@EnableJpaRepositories(basePackages = "com.cashly.cashly_api")
@ComponentScan(basePackages = "com.cashly.cashly_api")
public class TestJpaConfig {
}