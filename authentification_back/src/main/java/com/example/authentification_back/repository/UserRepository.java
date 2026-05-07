package com.example.authentification_back.repository;

import com.example.authentification_back.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Accès données pour {@link User} : requêtes dérivées Spring Data JPA.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	/**
	 * Recherche un utilisateur par email.
	 *
	 * @param email email normalisé
	 * @return utilisateur trouvé, sinon vide
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Recherche un utilisateur par jeton de session.
	 *
	 * @param token jeton persistant
	 * @return utilisateur trouvé, sinon vide
	 */
	Optional<User> findByToken(String token);

	/**
	 * Vérifie si un email est déjà pris.
	 *
	 * @param email email normalisé
	 * @return true si un utilisateur existe déjà avec cet email
	 */
	boolean existsByEmail(String email);
}
