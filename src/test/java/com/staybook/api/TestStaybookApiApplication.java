package com.staybook.api;

import org.springframework.boot.SpringApplication;

public class TestStaybookApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(StaybookApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
