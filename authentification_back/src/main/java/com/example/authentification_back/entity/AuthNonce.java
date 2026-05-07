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
 * Nonce anti-rejeu par utilisateur — contrainte unique {@code (user_id, nonce)} (énoncé TP3).
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

	/** @return identifiant technique du nonce */
	public Long getId() {
		return id;
	}

	/** @param id identifiant technique du nonce */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return identifiant de l'utilisateur propriétaire du nonce */
	public Long getUserId() {
		return userId;
	}

	/** @param userId identifiant de l'utilisateur propriétaire du nonce */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/** @return valeur aléatoire anti-rejeu */
	public String getNonce() {
		return nonce;
	}

	/** @param nonce valeur aléatoire anti-rejeu */
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	/** @return instant d'expiration du nonce */
	public Instant getExpiresAt() {
		return expiresAt;
	}

	/** @param expiresAt instant d'expiration du nonce */
	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	/** @return true si le nonce a déjà été consommé */
	public boolean isConsumed() {
		return consumed;
	}

	/** @param consumed true pour marquer le nonce comme consommé */
	public void setConsumed(boolean consumed) {
		this.consumed = consumed;
	}

	/** @return date de création en base */
	public Instant getCreatedAt() {
		return createdAt;
	}

	/** @param createdAt date de création en base */
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
