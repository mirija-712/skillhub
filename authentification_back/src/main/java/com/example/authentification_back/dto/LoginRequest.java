package com.example.authentification_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Connexion SkillHub : email + mot de passe.
 */
public record LoginRequest(
		@NotBlank @Email String email,
		@NotBlank String mot_de_passe
) {
}
