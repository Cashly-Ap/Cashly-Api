package com.cashly.cashly_api;

import org.springframework.boot.SpringApplication;

public class TestCashlyApplication {

	public static void main(String[] args) {
		SpringApplication.from(CashlyApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
