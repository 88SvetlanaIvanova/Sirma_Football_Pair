package com.sirma.football;

import org.springframework.boot.SpringApplication;

public class TestFootballApplication {

	public static void main(String[] args) {
		SpringApplication.from(FootballApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
