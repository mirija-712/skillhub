package com.example.authentification_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Données d’entrée pour la création de compte (alignement formulaire SkillHub / Laravel).
 * <p>
 * <b>Rôle</b> : porter les contraintes Bean Validation et les alias Jackson ({@code password}, etc.) avant traitement service.
 *
 * @param email adresse unique de connexion ; validée format email
 * @param mot_de_passe mot de passe principal (JSON {@code mot_de_passe} ou alias {@code password})
 * @param confirm_mot_de_passe confirmation obligatoire pour éviter les erreurs de saisie
 * @param nom nom de famille affiché (max 100 caractères)
 * @param prenom prénom optionnel (max 100 caractères)
 * @param role soit {@code participant} soit {@code formateur}
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public record RegisterRequest(
		@NotBlank(message = "L'email est obligatoire")
		@Email(message = "Format d'email invalide")
		String email,
		@NotBlank(message = "Le mot de passe est obligatoire")
		@Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
		@JsonProperty("mot_de_passe")
		@JsonAlias({"password"})
		String mot_de_passe,
		@NotBlank(message = "La confirmation du mot de passe est obligatoire")
		@JsonProperty("confirm_mot_de_passe")
		@JsonAlias({"passwordConfirm", "confirmPassword"})
		String confirm_mot_de_passe,
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
