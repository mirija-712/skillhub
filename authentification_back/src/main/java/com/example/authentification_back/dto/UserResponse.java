package com.example.authentification_back.dto;

import com.example.authentification_back.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Vue de sortie commune pour inscription, connexion et lecture profil (sérialisation Jackson).
 * <p>
 * <b>Rôle</b> : éviter d’exposer l’entité JPA et maîtriser les champs ({@code token} seulement après login).
 *
 * @param id identifiant utilisateur public
 * @param email email de connexion
 * @param nom nom affiché obligatoire métier
 * @param prenom prénom optionnel
 * @param role libellé métier ({@code participant} ou {@code formateur})
 * @param createdAt instant de création exposé au client
 * @param token jeton opaque de session ; {@code null} lorsque la réponse est un simple profil
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(Long id, String email, String nom, String prenom, String role, Instant createdAt, String token) {

	/**
	 * Fabrique une réponse « profil » après inscription ou pour {@code /me} sans divulguer de jeton dans le corps.
	 *
	 * @param user entité persistée dont on copie les champs non sensibles
	 * @return record avec {@code token} explicitement {@code null} (omis en JSON grâce à {@link JsonInclude})
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
	 * Fabrique la réponse post-authentification incluant le jeton que le client réutilisera dans les en-têtes.
	 *
	 * @param user entité fraîchement mise à jour avec le même jeton persisté
	 * @param token valeur UUID ou opaque à retourner au client (doit matcher la colonne {@code token})
	 * @return DTO sérialisable avec champ {@code token} renseigné pour les appels suivants
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
