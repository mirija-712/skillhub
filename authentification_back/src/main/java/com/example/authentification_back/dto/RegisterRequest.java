package com.example.authentification_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Corps JSON compatible avec le formulaire SkillHub. */
public record RegisterRequest(
		@NotBlank(message = "L'email est obligatoire")
		@Email(message = "Format d'email invalide")
		String email,
		@NotBlank(message = "Le mot de passe est obligatoire")
		@Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
		String mot_de_passe,
		@NotBlank(message = "Le nom est obligatoire")
		@Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
		String nom,
		@Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
		String prenom,
		@NotBlank(message = "Le rôle est obligatoire")
		@Pattern(regexp = "participant|formateur", message = "Le rôle doit être participant ou formateur")
		String role
) {
}
