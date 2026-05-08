package com.example.authentification_back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Ligne de persistance pour tracer un nonce SSO par utilisateur avec TTL et état consommé.
 * <p>
 * <b>Rôle</b> : garantir via contrainte SQL {@code (user_id, nonce)} qu’un même nonce ne soit pas rejoué pour le même compte.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@Entity
@Table(name = "auth_nonce", uniqueConstraints = @UniqueConstraint(name = "uk_auth_nonce_user_nonce", columnNames = {
		"user_id", "nonce"
}))
public class AuthNonce {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(nullable = false, length = 128)
	private String nonce;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean consumed;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void prePersist() {
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}

	/**
	 * Clé primaire technique du nonce.
	 *
	 * @return identifiant auto-généré
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Assigne l’identifiant surrogate (tests ou mappings manuels).
	 *
	 * @param id valeur à persister
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Référence l’utilisateur auquel le nonce est lié dans le protocole SSO.
	 *
	 * @return {@code utilisateurs.id} propriétaire
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * Associe le nonce à un compte applicatif précis.
	 *
	 * @param userId identifiant utilisateur valide existant
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * Valeur opaque échangée entre client et serveur pour éviter la duplication de requêtes signées.
	 *
	 * @return chaîne nonce persistée
	 */
	public String getNonce() {
		return nonce;
	}

	/**
	 * Définit le nonce émis lors de la passe d’authentification fédérée.
	 *
	 * @param nonce valeur respectant la contrainte d’unicité couplée au {@code userId}
	 */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	/**
	 * Limite temporelle après laquelle le nonce doit être considéré comme invalide.
	 *
	 * @return instant d’expiration absolu
	 */
	public Instant getExpiresAt() {
		return expiresAt;
	}

	/**
	 * Configure la fenêtre de validité pour les jobs de nettoyage ou la validation live.
	 *
	 * @param expiresAt borne haute de validité
	 */
	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	/**
	 * Indique si une consommation unique du nonce a déjà eu lieu.
	 *
	 * @return {@code true} lorsque le flux SSO a invalidé l’entrée pour anti-rejeu
	 */
	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Marque le nonce comme utilisé pour empêcher une seconde présentation identique.
	 *
	 * @param consumed {@code true} après traitement réussi côté serveur
	 */
	public void setConsumed(boolean consumed) {
		this.consumed = consumed;
	}

	/**
	 * Horodatage d’insertion du nonce (audit / TTL).
	 *
	 * @return {@code created_at}
	 */
	public Instant getCreatedAt() {
		return createdAt;
	}

	/**
	 * Fixe la date de création lorsque les callbacks JPA ne suffisent pas (jeux de données).
	 *
	 * @param createdAt instant d’insertion logique
	 */
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
