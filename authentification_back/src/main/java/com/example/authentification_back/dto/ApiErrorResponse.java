package com.example.authentification_back.dto;

import java.time.Instant;

/**
 * Contrat JSON unique pour les erreurs HTTP renvoyées par {@link com.example.authentification_back.exception.GlobalExceptionHandler}.
 * <p>
 * <b>Rôle</b> : offrir aux clients un schéma stable (timestamp, code, libellé HTTP, message fonctionnel, chemin demandé).
 *
 * @param timestamp instant UTC de construction de la réponse d’erreur
 * @param status code HTTP numérique (ex. 400, 401, 409)
 * @param error libellé standard du statut (ex. {@code Bad Request})
 * @param message détail lisible pour l’utilisateur ou les journaux applicatifs
 * @param path URI relative ou absolue appelée ({@link jakarta.servlet.http.HttpServletRequest#getRequestURI()})
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String message,
		String path
) {
}
