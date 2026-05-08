package com.example.authentification_back.exception;

/**
 * Verrouillage temporaire du compte après dépassement du seuil d’échecs de connexion (HTTP 423 Locked).
 * <p>
 * <b>Rôle</b> : informer le client qu’il doit attendre la fin de {@code lock_until} plutôt que réessayer immédiatement.
 * Le code <strong>423</strong> signale une ressource « gelée » ; <strong>429</strong> resterait une alternative sémantique de throttling.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public class AccountLockedException extends RuntimeException {

	/**
	 * Associe un message utilisateur au blocage en cours (durée configurable côté propriétés).
	 *
	 * @param message indication fonctionnelle affichée dans {@link com.example.authentification_back.dto.ApiErrorResponse}
	 */
	public AccountLockedException(String message) {
		super(message);
	}
}
