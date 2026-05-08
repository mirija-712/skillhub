package com.example.authentification_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée Spring Boot du service d'authentification SkillHub.
 * <p>
 * <b>Rôle</b> : démarrer le contexte applicatif, charger la configuration et exposer les beans
 * (contrôleurs, services, accès données). Au démarrage, Spring scanne ce package et les sous-packages
 * ({@code controller}, {@code service}, {@code repository}, {@code config}, …). La datasource et le port
 * sont dans {@code application.properties} (base MySQL partagée avec Laravel, table {@code utilisateurs}).
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class AuthentificationBackApplication {

	/**
	 * Lance l'application Spring Boot et démarre le serveur embarqué selon la configuration.
	 *
	 * @param args arguments JVM passés au démarrage (profils Spring, overrides de propriétés, etc.) ;
	 *             non interprétés par défaut par ce module hors mécanismes Spring Boot standard
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthentificationBackApplication.class, args);
	}
}
