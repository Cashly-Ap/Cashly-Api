package com.parsun.parsun_api;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan(basePackages = "com.parsun.parsun_api")
@EnableJpaRepositories(basePackages = "com.parsun.parsun_api")
@ComponentScan(basePackages = "com.parsun.parsun_api")
public class TestJpaConfig {
}