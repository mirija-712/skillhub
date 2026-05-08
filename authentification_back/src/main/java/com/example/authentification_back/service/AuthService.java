package com.example.authentification_back.service;

import com.example.authentification_back.config.AuthSecurityProperties;
import com.example.authentification_back.dto.ChangePasswordRequest;
import com.example.authentification_back.dto.LoginRequest;
import com.example.authentification_back.dto.RegisterRequest;
import com.example.authentification_back.dto.UserResponse;
import com.example.authentification_back.entity.User;
import com.example.authentification_back.exception.AccountLockedException;
import com.example.authentification_back.exception.AuthenticationFailedException;
import com.example.authentification_back.exception.InvalidInputException;
import com.example.authentification_back.exception.ResourceConflictException;
import com.example.authentification_back.repository.UserRepository;
import com.example.authentification_back.validation.PasswordPolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Logique métier d'authentification SkillHub.
 * <p>
 * <b>Rôle</b> : centraliser les règles d'inscription, de connexion avec gestion du verrouillage,
 * de résolution de session par jeton opaque et de changement de mot de passe conforme à la politique.
 * <b>Inscription</b> : contrôle d'unicité email, hash BCrypt du mot de passe, persistance des champs profil.
 * <b>Connexion</b> : vérifie le mot de passe, gère le verrouillage après échecs ({@link com.example.authentification_back.config.AuthSecurityProperties}),
 * génère un jeton de session (UUID) stocké en base.
 * <b>Profil</b> : {@code currentUser} résout l'utilisateur par ce jeton.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@Service
public class AuthService {

	/** Message volontairement générique pour éviter la divulgation d'information à la connexion. */
	public static final String GENERIC_LOGIN_ERROR = "Identifiants invalides";

	/** Message API pour changement de mot de passe : ancien mot de passe incorrect (tests + cohérence). */
	public static final String CHANGE_OLD_CREDENTIAL_ERROR = "Ancien mot de passe incorrect";

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final AuthSecurityProperties authProperties;
	private final PasswordPolicyValidator passwordPolicyValidator;
	private final Clock clock;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * Construit le service avec ses dépendances injectées (persistance, configuration sécurité, validation, temps).
	 *
	 * @param userRepository accès lecture/écriture aux {@link com.example.authentification_back.entity.User}
	 * @param authProperties seuils de verrouillage et durée de blocage après échecs répétés
	 * @param passwordPolicyValidator contrôle de complexité des mots de passe à la création et au changement
	 * @param clock horloge utilisée pour {@link Instant} (verrouillage, tests déterministes)
	 */
	public AuthService(
			UserRepository userRepository,
			AuthSecurityProperties authProperties,
			PasswordPolicyValidator passwordPolicyValidator,
			Clock clock) {
		this.userRepository = userRepository;
		this.authProperties = authProperties;
		this.passwordPolicyValidator = passwordPolicyValidator;
		this.clock = clock;
	}

	/**
	 * Inscrit un nouvel utilisateur : unicité email, validation métier, hash BCrypt et persistance JPA.
	 *
	 * @param request DTO d'inscription déjà validé côté contrôleur (Bean Validation)
	 * @return représentation profil sans jeton ({@link UserResponse#profile})
	 * @throws ResourceConflictException si {@link UserRepository#existsByEmail(String)} est vrai pour l'email normalisé
	 * @throws InvalidInputException si rôle hors périmètre, confirmation de mot de passe ou politique de mot de passe non respectée
	 */
	@Transactional
	public UserResponse register(RegisterRequest request) {
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			log.warn("Inscription échouée: email déjà utilisé ({})", email);
			throw new ResourceConflictException("Cet email est déjà enregistré");
		}

		if (request.role() == null || !(request.role().equals("participant") || request.role().equals("formateur"))) {
			throw new InvalidInputException("Le rôle doit être participant ou formateur");
		}
		if (!request.mot_de_passe().equals(request.confirm_mot_de_passe())) {
			throw new InvalidInputException("Les mots de passe ne correspondent pas");
		}
		passwordPolicyValidator.assertCompliant(request.mot_de_passe());

		User user = new User();
		user.setEmail(email);
		user.setMotDePasse(passwordEncoder.encode(request.mot_de_passe()));
		user.setNom(safeTrim(request.nom()));
		user.setPrenom(safeTrim(request.prenom()));
		user.setRole(request.role());
		userRepository.save(user);

		log.info("Inscription réussie pour l'utilisateur id={} email={}", user.getId(), email);
		return UserResponse.profile(user);
	}

	/**
	 * Authentifie un utilisateur : vérifie verrouillage, mot de passe BCrypt, réinitialise les échecs et pose un nouveau jeton UUID.
	 *
	 * @param request couple email / mot de passe tel que reçu du client
	 * @return profil enrichi du jeton courant ({@link UserResponse#login})
	 * @throws AuthenticationFailedException si email inconnu ou mot de passe incorrect (message générique volontaire)
	 * @throws AccountLockedException si {@code lockUntil} est encore dans le futur par rapport à {@link #clock}
	 */
	@Transactional(noRollbackFor = AuthenticationFailedException.class)
	public UserResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		Instant now = clock.instant();

		Optional<User> optUser = userRepository.findByEmail(email);
		if (optUser.isEmpty()) {
			log.warn("Connexion échouée: email non reconnu");
			throw new AuthenticationFailedException(GENERIC_LOGIN_ERROR);
		}
		User user = optUser.get();

		assertNotLocked(user, now);
		clearExpiredLockIfNeeded(user, now);

		if (!passwordEncoder.matches(request.mot_de_passe(), user.getMotDePasse())) {
			log.warn("Connexion échouée: mot de passe invalide (tentative {}/{})",
					user.getFailedLoginAttempts() + 1, authProperties.getMaxFailedAttempts());
			registerFailureAndThrow(user, email, now);
		}

		return grantSession(user, email);
	}

	private void assertNotLocked(User user, Instant now) {
		if (user.getLockUntil() != null && user.getLockUntil().isAfter(now)) {
			log.warn("Connexion refusée: compte verrouillé id={}", user.getId());
			throw new AccountLockedException("Compte temporairement verrouillé. Réessayez plus tard.");
		}
	}

	private void clearExpiredLockIfNeeded(User user, Instant now) {
		if (user.getLockUntil() != null && !user.getLockUntil().isAfter(now)) {
			user.setLockUntil(null);
			user.setFailedLoginAttempts(0);
			userRepository.save(user);
		}
	}

	private UserResponse grantSession(User user, String email) {
		user.setFailedLoginAttempts(0);
		user.setLockUntil(null);
		String newToken = UUID.randomUUID().toString();
		user.setToken(newToken);
		userRepository.save(user);
		log.info("Connexion réussie pour l'utilisateur id={} email={}", user.getId(), email);
		return UserResponse.login(user, newToken);
	}

	private void registerFailureAndThrow(User user, String email, Instant now) {
		int failures = user.getFailedLoginAttempts() + 1;
		user.setFailedLoginAttempts(failures);
		if (failures >= authProperties.getMaxFailedAttempts()) {
			user.setLockUntil(now.plus(authProperties.getLockDuration()));
			log.warn("Compte verrouillé après {} échecs id={} email={}", failures, user.getId(), email);
		}
		userRepository.save(user);
		log.warn("Connexion échouée: identifiants invalides (tentative {}/{})", failures, authProperties.getMaxFailedAttempts());
		throw new AuthenticationFailedException(GENERIC_LOGIN_ERROR);
	}

	/**
	 * Résout l'utilisateur par jeton opaque persisté et renvoie son profil public (sans renvoyer le jeton).
	 *
	 * @param rawToken jeton tel qu'extrait des en-têtes HTTP (espaces éventuellement présents, sera trim)
	 * @return DTO profil si une ligne {@link User} correspond au jeton
	 * @throws AuthenticationFailedException si jeton null/blanc ou aucune correspondance en base
	 */
	@Transactional(readOnly = true)
	public UserResponse currentUser(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new AuthenticationFailedException("Authentification requise");
		}
		String token = rawToken.trim();
		return userRepository.findByToken(token)
				.map(UserResponse::profile)
				.orElseThrow(() -> new AuthenticationFailedException("Token invalide"));
	}

	/**
	 * Met à jour le mot de passe après vérification de l'ancien et conformité du nouveau (politique + confirmation).
	 *
	 * @param rawToken jeton de session permettant de retrouver l'utilisateur ({@link #requireUserByToken(String)})
	 * @param request ancien mot de passe en clair, nouveau et confirmation
	 * @return aucune valeur ; effet de bord : enregistrement du nouveau hash BCrypt pour l'utilisateur courant
	 * @throws AuthenticationFailedException si jeton absent ou utilisateur introuvable
	 * @throws InvalidInputException si ancien mot de passe incorrect, confirmation différente ou politique non respectée
	 */
	@Transactional
	public void changePassword(String rawToken, ChangePasswordRequest request) {
		User user = requireUserByToken(rawToken);

		if (!passwordEncoder.matches(request.oldPassword(), user.getMotDePasse())) {
			log.warn("Changement mot de passe refusé: ancien mot de passe invalide user id={}", user.getId());
			throw new InvalidInputException(CHANGE_OLD_CREDENTIAL_ERROR);
		}
		if (!request.newPassword().equals(request.confirmPassword())) {
			throw new InvalidInputException("Les mots de passe ne correspondent pas");
		}
		passwordPolicyValidator.assertCompliant(request.newPassword());

		user.setMotDePasse(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
		log.info("Changement mot de passe réussi user id={}", user.getId());
	}

	private User requireUserByToken(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			throw new AuthenticationFailedException("Authentification requise");
		}
		String token = rawToken.trim();
		return userRepository.findByToken(token)
				.orElseThrow(() -> new AuthenticationFailedException("Token invalide"));
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return "";
		}
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static String safeTrim(String value) {
		return value == null ? null : value.trim();
	}
}
