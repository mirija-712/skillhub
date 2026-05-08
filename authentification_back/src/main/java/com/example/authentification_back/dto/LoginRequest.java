package com.example.authentification_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Charge utile minimale pour authentifier un utilisateur existant.
 * <p>
 * <b>Rôle</b> : transporter les identifiants vers {@link com.example.authentification_back.service.AuthService#login(LoginRequest)} après validation.
 *
 * @param email identifiant normalisé ensuite côté service (trim / casse)
 * @param mot_de_passe secret en clair comparé au hash BCrypt persisté
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String mot_de_passe
) {
}
