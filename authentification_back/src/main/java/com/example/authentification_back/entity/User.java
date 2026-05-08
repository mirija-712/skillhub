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
 * Entité JPA représentant un utilisateur applicatif persisté dans {@code utilisateurs} (schéma partagé Laravel).
 * <p>
 * <b>Rôle</b> : porter l’état métier (identité, rôle), le secret chiffré (BCrypt), le jeton de session et les champs
 * de sécurité ({@code failed_login_attempts}, {@code lock_until}) ; les dates sont auto-renseignées avant écriture.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
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

	/**
	 * Identifiant surrogate auto-incrémenté par MySQL.
	 *
	 * @return valeur de la colonne {@code id}
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Fixe l’identifiant (réservé à JPA, séquences de tests ou imports).
	 *
	 * @param id nouvelle clé primaire
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Email canonique de connexion (contrainte unique).
	 *
	 * @return chaîne persistée telle qu’en base après normalisation métier éventuelle
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Met à jour l’email lors de la création ou d’un futur flux de changement d’email.
	 *
	 * @param email valeur unique attendue par les index SQL
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Secret d’authentification stocké sous forme de hash BCrypt (jamais en clair).
	 *
	 * @return empreinte BCrypt compatible {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder}
	 */
	public String getMotDePasse() {
		return motDePasse;
	}

	/**
	 * Remplace le hash (inscription, changement de mot de passe ou migrations).
	 *
	 * @param motDePasse nouveau hash déjà encodé ; ne pas passer le mot de passe brut ici
	 */
	public void setMotDePasse(String motDePasse) {
		this.motDePasse = motDePasse;
	}

	/**
	 * Nom de famille obligatoire dans les formulaires SkillHub.
	 *
	 * @return valeur colonne {@code nom}
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Met à jour le nom affiché sur les écrans et rapports.
	 *
	 * @param nom chaîne non {@code null} côté contrainte nullable=false
	 */
	public void setNom(String nom) {
		this.nom = nom;
	}

	/**
	 * Prénom optionnel pour personnaliser l’accueil utilisateur.
	 *
	 * @return prénom ou {@code null} si absent en base
	 */
	public String getPrenom() {
		return prenom;
	}

	/**
	 * Définit ou efface le prénom selon les données fournies au formulaire.
	 *
	 * @param prenom valeur pouvant être {@code null} si non renseignée
	 */
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}

	/**
	 * Rôle fonctionnel limité aux valeurs métier {@code participant} et {@code formateur}.
	 *
	 * @return étiquette persistée dans {@code role}
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Positionne le périmètre de droits UI / API associé au compte.
	 *
	 * @param role valeur contrôlée côté service avant persistance
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Instant de première insertion, renseigné par {@code @PrePersist} si absent.
	 *
	 * @return {@code created_at} tel que stocké en base
	 */
	public Instant getCreatedAt() {
		return createdAt;
	}

	/**
	 * Permet d’outrepasser la date de création pour imports ou tests (usage rare).
	 *
	 * @param createdAt nouvelle valeur logique de création
	 */
	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Horodatage de dernière modification, rafraîchi à chaque {@code @PreUpdate}.
	 *
	 * @return colonne {@code updated_at}
	 */
	public Instant getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Met à jour manuellement la date de modification (normalement gérée par les callbacks).
	 *
	 * @param updatedAt instant à persister
	 */
	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * Jeton opaque de session actif ; {@code null} si aucune session ouverte.
	 *
	 * @return UUID ou chaîne égale à celle renvoyée au client lors du dernier login réussi
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Pose ou invalide le jeton (nouvelle connexion, déconnexion future, rotation).
	 *
	 * @param token valeur unique côté contrainte d’unicité SQL ou {@code null}
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Compteur d’échecs consécutifs de mot de passe utilisé pour déclencher le lockout.
	 *
	 * @return entier remis à zéro après login réussi ou après expiration du lock
	 */
	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	/**
	 * Ajuste le compteur lors du traitement d’une tentative de connexion invalide ou d’une réinitialisation.
	 *
	 * @param failedLoginAttempts nombre d’échecs à persister
	 */
	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	/**
	 * Borne temporelle au-delà de laquelle le compte redevient joignable si {@code null} ou passée.
	 *
	 * @return instant futur de fin de blocage, ou {@code null} si pas de lock actif
	 */
	public Instant getLockUntil() {
		return lockUntil;
	}

	/**
	 * Programme ou annule le gel du compte (via {@code null}) après calcul métier.
	 *
	 * @param lockUntil instant limite ; {@code null} supprime le verrouillage
	 */
	public void setLockUntil(Instant lockUntil) {
		this.lockUntil = lockUntil;
	}
}
