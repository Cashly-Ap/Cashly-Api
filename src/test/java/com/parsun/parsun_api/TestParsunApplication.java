package com.parsun.parsun_api;

import org.springframework.boot.SpringApplication;

public class TestParsunApplication {

	public static void main(String[] args) {
		SpringApplication.from(ParsunApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
