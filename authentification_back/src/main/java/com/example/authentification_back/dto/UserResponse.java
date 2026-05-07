package com.example.authentification_back.dto;

import com.example.authentification_back.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Réponse JSON pour inscription, login et profil.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id, String email, String nom, String prenom, String role, Instant createdAt, String token) {

	/**
	 * Construit une réponse profil (sans jeton).
	 *
	 * @param user entité utilisateur source
	 * @return réponse profil sérialisable en JSON
	 */
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

	/**
	 * Construit une réponse de connexion avec jeton.
	 *
	 * @param user entité utilisateur source
	 * @param token jeton de session à renvoyer au client
	 * @return réponse de connexion sérialisable en JSON
	 */
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
