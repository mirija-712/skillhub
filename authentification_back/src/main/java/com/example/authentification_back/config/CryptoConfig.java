package com.example.authentification_back.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Beans transverses liés au temps et au chargement des propriétés de sécurité.
 * <p>
 * <b>Rôle</b> : exposer une {@link Clock} injectable pour des tests déterministes du verrouillage de compte,
 * et activer le binding {@link AuthSecurityProperties}.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@Configuration
@EnableConfigurationProperties(AuthSecurityProperties.class)
public class CryptoConfig {

	/**
	 * Horloge partagée pour mesurer {@link java.time.Instant} de manière cohérente dans les services.
	 *
	 * @return implémentation JVM {@link Clock#systemUTC()} comme bean singleton Spring
	 */
	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
}
