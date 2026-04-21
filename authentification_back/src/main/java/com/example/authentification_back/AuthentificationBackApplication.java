package com.example.authentification_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée Spring Boot du service d'authentification SkillHub.
 * <p>
 * Au démarrage, Spring scanne ce package et les sous-packages ({@code controller}, {@code service},
 * {@code repository}, {@code config}, …). La datasource et le port sont dans {@code application.properties}
 * (base MySQL partagée avec Laravel, typiquement table {@code utilisateurs}).
 */
@SpringBootApplication
public class AuthentificationBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthentificationBackApplication.class, args);
	}
}
