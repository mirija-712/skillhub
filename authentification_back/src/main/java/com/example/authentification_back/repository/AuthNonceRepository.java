package com.example.authentification_back.repository;

import com.example.authentification_back.entity.AuthNonce;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès données des nonces anti-rejeu utilisés par le flux SSO.
 */
public interface AuthNonceRepository extends JpaRepository<AuthNonce, Long> {

	/**
	 * Vérifie l'existence d'un nonce déjà utilisé pour un utilisateur.
	 *
	 * @param userId identifiant utilisateur
	 * @param nonce nonce reçu dans la requête
	 * @return true si ce couple utilisateur/nonce existe déjà
	 */
	boolean existsByUserIdAndNonce(Long userId, String nonce);
}
