package com.example.authentification_back.exception;

/**
 * Indique qu’une création ou mise à jour entre en collision avec l’état existant (HTTP 409 Conflict).
 * <p>
 * <b>Rôle</b> : exposer proprement les violations d’unicité métier sans les confondre avec des erreurs de validation générique.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public class ResourceConflictException extends RuntimeException {

	/**
	 * Crée le conflit avec un message compréhensible côté API (ex. email déjà enregistré).
	 *
	 * @param message détail sérialisé dans le corps JSON d’erreur
	 */
	public ResourceConflictException(String message) {
		super(message);
	}
}
