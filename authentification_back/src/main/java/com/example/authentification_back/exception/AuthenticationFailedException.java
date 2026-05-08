package com.example.authentification_back.exception;

/**
 * Erreur d’identité ou de jeton : utilisateur non authentifié ou session invalide (HTTP 401).
 * <p>
 * <b>Rôle</b> : faire échouer explicitement les chemins protégés sans révéler le détail interne des causes système.
 * En production, compléter par expiration / révocation des jetons ; le TP reste volontairement minimaliste.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public class AuthenticationFailedException extends RuntimeException {

	/**
	 * Crée une erreur d’auth avec un message exposé au client (parfois volontairement générique).
	 *
	 * @param message raison fonctionnelle traduite en réponse JSON ({@code Unauthorized})
	 */
	public AuthenticationFailedException(String message) {
		super(message);
	}
}
