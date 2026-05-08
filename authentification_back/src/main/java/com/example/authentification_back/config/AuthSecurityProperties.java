package com.example.authentification_back.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Paramètres externalisés {@code app.auth.*} pour verrouillage de compte et fenêtre SSO / nonce (TP2–TP3).
 * <p>
 * <b>Rôle</b> : régler sans recompilation les seuils de sécurité consommés par les services et futurs contrôleurs SSO.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@ConfigurationProperties(prefix = "app.auth")
public class AuthSecurityProperties {

	private Duration lockDuration = Duration.ofMinutes(2);

	private int maxFailedAttempts = 5;

	/** Fenêtre acceptée autour de l’heure serveur pour {@code timestamp} (± secondes, défaut 60). */
	private int timestampSkewSeconds = 60;

	/** Durée de conservation du nonce en base (≈ now + 2 min à l’énoncé). */
	private int nonceTtlSeconds = 120;

	/**
	 * Durée pendant laquelle les nouvelles connexions sont refusées après trop de mots de passe incorrects.
	 *
	 * @return fenêtre de blocage appliquée lorsque {@code failed_login_attempts} dépasse le seuil configuré
	 */
	public Duration getLockDuration() {
		return lockDuration;
	}

	/**
	 * Définit la durée de gel du compte après lockout (binding depuis YAML ou propriétés).
	 *
	 * @param lockDuration nouvelle valeur ; typiquement quelques minutes en démo
	 */
	public void setLockDuration(Duration lockDuration) {
		this.lockDuration = lockDuration;
	}

	/**
	 * Plafond de tentatives échouées avant application de {@link #getLockDuration()}.
	 *
	 * @return nombre d’échecs consécutifs tolérés (valeur par défaut du TP : 5)
	 */
	public int getMaxFailedAttempts() {
		return maxFailedAttempts;
	}

	/**
	 * Positionne le seuil métier de déclenchement du verrouillage automatique.
	 *
	 * @param maxFailedAttempts nombre strictement positif attendu par la configuration
	 */
	public void setMaxFailedAttempts(int maxFailedAttempts) {
		this.maxFailedAttempts = maxFailedAttempts;
	}

	/**
	 * Marge acceptée entre l’horloge client et serveur lors de la validation de signatures horodatées (TP3).
	 *
	 * @return nombre de secondes de tolérance de part et d’autre de l’instant serveur
	 */
	public int getTimestampSkewSeconds() {
		return timestampSkewSeconds;
	}

	/**
	 * Configure la fenêtre temporelle symétrique autorisée pour les flux SSO.
	 *
	 * @param timestampSkewSeconds valeur positive exprimée en secondes
	 */
	public void setTimestampSkewSeconds(int timestampSkewSeconds) {
		this.timestampSkewSeconds = timestampSkewSeconds;
	}

	/**
	 * Durée maximale de validité d’un nonce persisté avant purge ou rejet logique.
	 *
	 * @return TTL du nonce en secondes depuis création côté serveur
	 */
	public int getNonceTtlSeconds() {
		return nonceTtlSeconds;
	}

	/**
	 * Fixe la TTL des nonces pour limiter la fenêtre d’attaque par rejeu.
	 *
	 * @param nonceTtlSeconds durée de conservation acceptable en secondes
	 */
	public void setNonceTtlSeconds(int nonceTtlSeconds) {
		this.nonceTtlSeconds = nonceTtlSeconds;
	}
}
