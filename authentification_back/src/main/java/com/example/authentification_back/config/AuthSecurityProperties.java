package com.example.authentification_back.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Lockout TP2 + fenêtre temporelle et TTL nonce TP3 (énoncé).
 */
@ConfigurationProperties(prefix = "app.auth")
public class AuthSecurityProperties {

	private Duration lockDuration = Duration.ofMinutes(2);

	private int maxFailedAttempts = 5;

	/** Fenêtre acceptée autour de l’heure serveur pour {@code timestamp} (± secondes, défaut 60). */
	private int timestampSkewSeconds = 60;

	/** Durée de conservation du nonce en base (≈ now + 2 min à l’énoncé). */
	private int nonceTtlSeconds = 120;

	/** @return durée de verrouillage d'un compte après dépassement des échecs */
	public Duration getLockDuration() {
		return lockDuration;
	}

	/** @param lockDuration durée de verrouillage d'un compte après dépassement des échecs */
	public void setLockDuration(Duration lockDuration) {
		this.lockDuration = lockDuration;
	}

	/** @return nombre maximum d'échecs de connexion autorisés avant verrouillage */
	public int getMaxFailedAttempts() {
		return maxFailedAttempts;
	}

	/** @param maxFailedAttempts nombre maximum d'échecs de connexion autorisés avant verrouillage */
	public void setMaxFailedAttempts(int maxFailedAttempts) {
		this.maxFailedAttempts = maxFailedAttempts;
	}

	/** @return tolérance de décalage temporel (en secondes) pour les signatures SSO */
	public int getTimestampSkewSeconds() {
		return timestampSkewSeconds;
	}

	/** @param timestampSkewSeconds tolérance de décalage temporel (en secondes) pour les signatures SSO */
	public void setTimestampSkewSeconds(int timestampSkewSeconds) {
		this.timestampSkewSeconds = timestampSkewSeconds;
	}

	/** @return durée de vie d'un nonce en secondes */
	public int getNonceTtlSeconds() {
		return nonceTtlSeconds;
	}

	/** @param nonceTtlSeconds durée de vie d'un nonce en secondes */
	public void setNonceTtlSeconds(int nonceTtlSeconds) {
		this.nonceTtlSeconds = nonceTtlSeconds;
	}
}
