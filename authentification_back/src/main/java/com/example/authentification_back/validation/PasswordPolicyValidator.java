package com.example.authentification_back.validation;

import com.example.authentification_back.exception.InvalidInputException;
import org.springframework.stereotype.Component;

/**
 * Politique de mot de passe du TP2 : au moins 12 caractères, une majuscule, une minuscule, un chiffre,
 * un caractère non alphanumérique (symbole).
 * <p>
 * <b>Rôle</b> : appliquer une barrière minimale homogène à l’inscription et au changement de mot de passe,
 * indépendamment des annotations Bean Validation sur les DTO (complément métier).
 * <p>
 * Cette implémentation est volontairement limitée au sens TP (pas de liste de mots courants, pas de zxcvbn).
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@Component
public class PasswordPolicyValidator {

	/** Longueur minimale requise pour les mots de passe. */
	public static final int MIN_LENGTH = 12;

	/**
	 * Vérifie que le mot de passe satisfait toutes les règles configurées ; sinon interrompt par une exception métier.
	 *
	 * @param password secret en clair provenant du client ou du batch de tests
	 * @return aucune valeur ; termine normalement uniquement si toutes les contraintes sont satisfaites
	 * @throws InvalidInputException si une règle de longueur ou de classes de caractères échoue (HTTP 400 via handler)
	 */
	public void assertCompliant(String password) {
		if (password == null || password.length() < MIN_LENGTH) {
			throw new InvalidInputException(
					"Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères");
		}
		if (!password.matches(".*[A-Z].*")) {
			throw new InvalidInputException("Le mot de passe doit contenir au moins une majuscule");
		}
		if (!password.matches(".*[a-z].*")) {
			throw new InvalidInputException("Le mot de passe doit contenir au moins une minuscule");
		}
		if (!password.matches(".*\\d.*")) {
			throw new InvalidInputException("Le mot de passe doit contenir au moins un chiffre");
		}
		// Tout caractère qui n'est ni lettre ni chiffre Unicode (symboles, ponctuation, etc.)
		if (password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
			throw new InvalidInputException("Le mot de passe doit contenir au moins un caractère spécial");
		}
	}
}
