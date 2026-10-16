package com.cashly.cashly_api.shared.infrastructure.bootstrap;

import com.cashly.cashly_api.shared.infrastructure.config.TestcontainersConfiguration;

import org.springframework.boot.SpringApplication;
import com.cashly.cashly_api.CashlyApplication;

public class TestCashlyApplication {

	public static void main(String[] args) {
		SpringApplication.from(CashlyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
