package com.example.authentification_back.dto;

import com.example.authentification_back.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Réponse JSON pour inscription, login et profil.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id, String email, String nom, String prenom, String role, Instant createdAt, String token) {

	/** Profil sans exposer le jeton (inscription, {@code GET /api/me}). */
	public static UserResponse profile(User user) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getNom(),
				user.getPrenom(),
				user.getRole(),
				user.getCreatedAt(),
				null
		);
	}

	/** Réponse de connexion : inclut le jeton à transmettre dans les en-têtes des requêtes suivantes. */
	public static UserResponse login(User user, String token) {
		return new UserResponse(
				user.getId(),
				user.getEmail(),
				user.getNom(),
				user.getPrenom(),
				user.getRole(),
				user.getCreatedAt(),
				token
		);
	}
}
