package com.example.authentification_back.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps JSON pour la mise à jour du mot de passe d’un utilisateur déjà authentifié par jeton.
 * <p>
 * <b>Rôle</b> : séparer ancien secret (vérification) et nouveau secret avec confirmation pour éviter les erreurs client.
 *
 * @param oldPassword mot de passe actuel à valider contre le hash stocké
 * @param newPassword nouveau secret soumis à la politique de complexité métier
 * @param confirmPassword doit être strictement égale à {@code newPassword}
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public record ChangePasswordRequest(
		@NotBlank(message = "L'ancien mot de passe est obligatoire")
		String oldPassword,
		@NotBlank(message = "Le nouveau mot de passe est obligatoire")
		String newPassword,
		@NotBlank(message = "La confirmation du mot de passe est obligatoire")
		String confirmPassword
) {
}
