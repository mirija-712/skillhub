package com.example.authentification_back.exception;

import com.example.authentification_back.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Advice MVC centralisant la traduction des exceptions applicatives et de validation en réponses JSON homogènes.
 * <p>
 * <b>Rôle</b> : garantir pour chaque erreur un corps {@link ApiErrorResponse} (timestamp, statut, libellé HTTP,
 * message métier, chemin) afin que clients et tests puissent parser un schéma unique (TP1 / TP2).
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Transforme une erreur de saisie ou de règle métier « bad request » en réponse 400 structurée.
	 *
	 * @param ex exception portant le message utilisateur affichable
	 * @param req requête courante pour renseigner le champ {@code path} du DTO d’erreur
	 * @return entité {@link ApiErrorResponse} encapsulée dans HTTP 400
	 */
	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalid(InvalidInputException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
	}

	/**
	 * Répond aux tentatives de connexion sur un compte encore sous délai de verrouillage (Locked).
	 *
	 * @param ex exception avec message invitant à réessayer ultérieurement
	 * @param req requête pour tracer l’URI appelée dans la réponse
	 * @return corps d’erreur avec statut HTTP 423 (WebDAV « Locked », utilisé ici comme compte gelé)
	 */
	@ExceptionHandler(AccountLockedException.class)
	public ResponseEntity<ApiErrorResponse> handleLocked(AccountLockedException ex, HttpServletRequest req) {
		return build(HttpStatus.LOCKED, ex.getMessage(), req);
	}

	/**
	 * Couvre jeton absent/invalide ou identifiants de connexion incorrects selon les services qui propagent {@link AuthenticationFailedException}.
	 *
	 * @param ex détail fonctionnel volontairement parfois générique pour limiter l’énumération d’utilisateurs
	 * @param req requête associée pour le champ {@code path}
	 * @return enveloppe JSON avec statut HTTP 401 Unauthorized
	 */
	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationFailedException ex, HttpServletRequest req) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
	}

	/**
	 * Répond aux violations d’unicité ou autres situations où la ressource existe déjà (ex. email pris).
	 *
	 * @param ex message décrivant le conflit côté client
	 * @param req requête pour journaliser le chemin dans la charge JSON
	 * @return réponse dont le code HTTP est 409 Conflict
	 */
	@ExceptionHandler(ResourceConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(ResourceConflictException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), req);
	}

	/**
	 * Agrège les erreurs {@code javax.validation} portées par {@link MethodArgumentNotValidException} en un seul message lisible.
	 *
	 * @param ex résultat de liaison avec liste des {@code FieldError}
	 * @param req requête HTTP servant au champ {@code path}
	 * @return réponse 400 dont le message concatène {@code champ: message} pour chaque erreur
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return build(HttpStatus.BAD_REQUEST, message.isEmpty() ? "Données invalides" : message, req);
	}

	private static ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				req.getRequestURI()
		);
		return ResponseEntity.status(status).body(body);
	}
}
