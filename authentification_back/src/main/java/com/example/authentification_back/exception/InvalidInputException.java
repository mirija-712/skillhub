package com.example.authentification_back.exception;

/**
 * Exception métier levée lorsque la requête ou les données fournies violent une règle fonctionnelle (HTTP 400).
 * <p>
 * <b>Rôle</b> : distinguer les erreurs « métier / formulaire » des erreurs d’authentification ou de conflit,
 * tout en laissant {@link GlobalExceptionHandler} uniformiser la réponse JSON.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public class InvalidInputException extends RuntimeException {

	/**
	 * Construit l’erreur avec un message destiné au client (après filtrage applicatif éventuel).
	 *
	 * @param message libellé détaillant la violation (ex. confirmation des mots de passe différente)
	 */
	public InvalidInputException(String message) {
		super(message);
	}
}
