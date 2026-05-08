package com.example.authentification_back.repository;

import com.example.authentification_back.entity.AuthNonce;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistance des enregistrements {@link AuthNonce} pour anti-rejeu par couple utilisateur / nonce.
 * <p>
 * <b>Rôle</b> : permettre au flux SSO de rejeter une réutilisation de nonce ou de détecter des collisions contrôlées.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
public interface AuthNonceRepository extends JpaRepository<AuthNonce, Long> {

	/**
	 * Indique si la demande courante réemploie un nonce déjà consigné pour cet utilisateur (protection rejeu).
	 *
	 * @param userId clé étrangère logique vers {@code utilisateurs.id}
	 * @param nonce valeur aléatoire ou opaque issue du client / broker SSO
	 * @return {@code true} si une ligne existe déjà pour ce couple ; {@code false} si première occurrence
	 * @throws org.springframework.dao.DataAccessException si la requête dérivée échoue côté datasource
	 */
	boolean existsByUserIdAndNonce(Long userId, String nonce);
}
