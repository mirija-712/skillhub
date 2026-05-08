package com.example.authentification_back.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS pour les navigateurs accédant à l’API depuis une autre origine locale.
 * <p>
 * <b>Rôle</b> : autoriser les méthodes REST usuelles et tous les en-têtes sur {@code /api/**} pour {@code localhost}.
 * Les clients non navigateur (JavaFX, tests curl même origine) ne dépendent pas de cette config.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Déclare les politiques Cross-Origin pour le préflight et les appels directs depuis un front sur un autre port.
	 *
	 * @param registry registre MVC où ajouter le mapping {@code /api/**}, origines et méthodes autorisées
	 * @return aucune valeur ; la configuration est enregistrée via mutation de {@code registry}
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*");
	}
}
