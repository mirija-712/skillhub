package com.example.authentification_back.repository;

import com.example.authentification_back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Couche de persistance Spring Data pour l’entité {@link User} (table {@code utilisateurs}).
 * <p>
 * <b>Rôle</b> : exposer les requêtes dérivées utilisées par {@link com.example.authentification_back.service.AuthService}
 * sans implémentation manuelle (proxy runtime Spring Data).
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Résout un utilisateur par son email fonctionnel (unique métier).
	 *
	 * @param email valeur déjà normalisée (trim / casse) pour correspondre à la colonne indexée
	 * @return ligne {@link User} encapsulée ou {@link Optional#empty()} si aucune correspondance
	 * @throws org.springframework.dao.DataAccessException si la requête SQL ou la session JPA échoue
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Résout la session courante via le jeton opaque stocké en base après connexion réussie.
	 *
	 * @param token UUID ou chaîne persistée dans {@code utilisateurs.token}
	 * @return utilisateur actif pour ce jeton, sinon vide si jeton révoqué ou inconnu
	 * @throws org.springframework.dao.DataAccessException si la couche persistence remonte une erreur d’accès données
	 */
	Optional<User> findByToken(String token);

	/**
	 * Teste l’unicité de l’email avant inscription pour éviter une contrainte SQL brutale.
	 *
	 * @param email même normalisation que pour {@link #findByEmail(String)}
	 * @return {@code true} si au moins une ligne porte cet email ; {@code false} si disponible pour création
	 * @throws org.springframework.dao.DataAccessException si le test d’existence ne peut pas être exécuté
	 */
	boolean existsByEmail(String email);
}
