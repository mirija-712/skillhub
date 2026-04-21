package com.example.authentification_back.controller;

import com.example.authentification_back.dto.ChangePasswordRequest;
import com.example.authentification_back.dto.LoginRequest;
import com.example.authentification_back.dto.RegisterRequest;
import com.example.authentification_back.dto.UserResponse;
import com.example.authentification_back.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur REST d'authentification pour SkillHub.
 * <p><b>Rôle</b> : exposer les opérations « compte utilisateur » sur la table {@code utilisateurs}
 * (même base MySQL que Laravel). Le front React appelle en priorité les chemins français
 * {@code /auth/inscription}, {@code /auth/connexion}, {@code /auth/me} ; les chemins anglais
 * ({@code /auth/register}, {@code /auth/login}, {@code /me}) restent des alias pour tests ou clients alternatifs.
 * <p><b>Jeton</b> : après connexion, un UUID est stocké en base et renvoyé au client ; les requêtes suivantes
 * l'envoient en {@code Authorization: Bearer …} ou {@code X-Auth-Token}. Laravel valide ce jeton via {@code GET /auth/me}.
 * <p><b>Erreurs</b> : format {@link com.example.authentification_back.dto.ApiErrorResponse} géré par
 * {@link com.example.authentification_back.exception.GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/auth/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		UserResponse body = authService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@PostMapping("/auth/inscription")
	public ResponseEntity<UserResponse> inscription(@Valid @RequestBody RegisterRequest request) {
		return register(request);
	}

	@PostMapping("/auth/login")
	public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/auth/connexion")
	public ResponseEntity<UserResponse> connexion(@Valid @RequestBody LoginRequest request) {
		return login(request);
	}

	@PutMapping("/auth/change-password")
	public ResponseEntity<Map<String, String>> changePassword(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestHeader(value = "X-Auth-Token", required = false) String authToken,
			@Valid @RequestBody ChangePasswordRequest request) {
		authService.changePassword(resolveToken(authorization, authToken), request);
		return ResponseEntity.ok(Map.of("message", "Mot de passe changé avec succès"));
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestHeader(value = "X-Auth-Token", required = false) String authToken) {
		String resolved = resolveToken(authorization, authToken);
		return ResponseEntity.ok(authService.currentUser(resolved));
	}

	@GetMapping("/auth/me")
	public ResponseEntity<UserResponse> authMe(
			@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
			@RequestHeader(value = "X-Auth-Token", required = false) String authToken) {
		return me(authorization, authToken);
	}

	private static String resolveToken(String authorization, String authToken) {
		String bearer = extractBearer(authorization);
		if (bearer != null && !bearer.isBlank()) {
			return bearer;
		}
		return authToken;
	}

	private static String extractBearer(String authorization) {
		if (authorization == null || authorization.isBlank()) {
			return null;
		}
		String trimmed = authorization.trim();
		if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
			return trimmed.substring(7).trim();
		}
		return trimmed;
	}
}
