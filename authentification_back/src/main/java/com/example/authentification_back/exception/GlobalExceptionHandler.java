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
 * Intercepte les exceptions métier et les erreurs de validation Bean Validation pour renvoyer
 * toujours un JSON {@link ApiErrorResponse} cohérent (TP1 / TP2).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Gère les erreurs métier de saisie.
	 *
	 * @param ex exception métier
	 * @param req requête HTTP en cours
	 * @return réponse normalisée en HTTP 400
	 */
	@ExceptionHandler(InvalidInputException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalid(InvalidInputException ex, HttpServletRequest req) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
	}

	/**
	 * Gère les comptes temporairement verrouillés.
	 *
	 * @param ex exception métier
	 * @param req requête HTTP en cours
	 * @return réponse normalisée en HTTP 423
	 */
	@ExceptionHandler(AccountLockedException.class)
	public ResponseEntity<ApiErrorResponse> handleLocked(AccountLockedException ex, HttpServletRequest req) {
		return build(HttpStatus.LOCKED, ex.getMessage(), req);
	}

	/**
	 * Gère les échecs d'authentification.
	 *
	 * @param ex exception métier
	 * @param req requête HTTP en cours
	 * @return réponse normalisée en HTTP 401
	 */
	@ExceptionHandler(AuthenticationFailedException.class)
	public ResponseEntity<ApiErrorResponse> handleAuth(AuthenticationFailedException ex, HttpServletRequest req) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
	}

	/**
	 * Gère les conflits métier (ex: email déjà utilisé).
	 *
	 * @param ex exception métier
	 * @param req requête HTTP en cours
	 * @return réponse normalisée en HTTP 409
	 */
	@ExceptionHandler(ResourceConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(ResourceConflictException ex, HttpServletRequest req) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), req);
	}

	/**
	 * Gère les erreurs de validation Bean Validation sur les DTO.
	 *
	 * @param ex exception de validation Spring
	 * @param req requête HTTP en cours
	 * @return réponse normalisée en HTTP 400
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
