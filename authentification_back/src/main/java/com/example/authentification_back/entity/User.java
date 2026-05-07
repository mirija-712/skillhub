package com.example.authentification_back.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entité JPA mappée sur la table MySQL {@code utilisateurs} (partagée avec Laravel).
 * Colonnes métier : email, mot_de_passe (hash BCrypt), nom, prenom, role.
 * Colonnes de session / sécurité : token (jeton opaque), compteurs de verrouillage (échecs connexion, lock_until).
 * Les dates created_at / updated_at sont remplies par les callbacks {@code @PrePersist} / {@code @PreUpdate}.
 */
@Entity
@Table(name = "utilisateurs")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "mot_de_passe", nullable = false, length = 255)
	private String motDePasse;

	@Column(name = "nom", nullable = false, length = 100)
	private String nom;

	@Column(name = "prenom", length = 100)
	private String prenom;

	@Column(name = "role", nullable = false, length = 20)
	private String role = "participant";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(unique = true, length = 64)
	private String token;

	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts = 0;

	@Column(name = "lock_until")
	private Instant lockUntil;

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		if (createdAt == null) createdAt = now;
		if (updatedAt == null) updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	/** @return identifiant technique de l'utilisateur */
	public Long getId() {
		return id;
	}

	/** @param id identifiant technique de l'utilisateur */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return email unique de connexion */
	public String getEmail() {
		return email;
	}

	/** @param email email unique de connexion */
	public void setEmail(String email) {
		this.email = email;
	}

	/** @return mot de passe haché (BCrypt) */
	public String getMotDePasse() {
		return motDePasse;
	}

	/** @param motDePasse mot de passe haché (BCrypt) */
	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	/** @return nom de famille */
	public String getNom() {
		return nom;
	}

	/** @param nom nom de famille */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/** @return prénom */
	public String getPrenom() {
		return prenom;
	}

	/** @param prenom prénom */
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	/** @return rôle applicatif (participant ou formateur) */
	public String getRole() {
		return role;
	}

	/** @param role rôle applicatif (participant ou formateur) */
	public void setRole(String role) {
		this.role = role;
	}

	/** @return date de création */
	public Instant getCreatedAt() {
		return createdAt;
	}

	/** @param createdAt date de création */
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	/** @return date de dernière mise à jour */
	public Instant getUpdatedAt() {
		return updatedAt;
	}

	/** @param updatedAt date de dernière mise à jour */
	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	/** @return jeton de session courant */
	public String getToken() {
		return token;
	}

	/** @param token jeton de session courant */
	public void setToken(String token) {
		this.token = token;
	}

	/** @return nombre d'échecs de connexion consécutifs */
	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	/** @param failedLoginAttempts nombre d'échecs de connexion consécutifs */
	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	/** @return instant jusqu'auquel le compte est verrouillé */
	public Instant getLockUntil() {
		return lockUntil;
	}

	/** @param lockUntil instant jusqu'auquel le compte est verrouillé */
	public void setLockUntil(Instant lockUntil) {
		this.lockUntil = lockUntil;
	}
}
