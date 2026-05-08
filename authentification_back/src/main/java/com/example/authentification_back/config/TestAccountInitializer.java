package com.example.authentification_back.config;

import com.example.authentification_back.entity.User;
import com.example.authentification_back.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Initialise au démarrage un compte de démonstration pour faciliter les TP et démos locales.
 * <p>
 * <b>Rôle</b> : si {@link #TEST_EMAIL} est absent de la base et qu’un secret d’environnement est disponible,
 * insérer un utilisateur avec mot de passe BCrypt dérivé de ce secret ; sinon journaliser et ignorer silencieusement.
 *
 * @author SkillHub
 * @version 0.0.1-SNAPSHOT
 */
@Component
public class TestAccountInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(TestAccountInitializer.class);

	/** Email du compte de démonstration créé automatiquement au démarrage. */
	public static final String TEST_EMAIL = "toto@example.com";
	private static final String TEST_ACCOUNT_SECRET_ENV = "TEST_ACCOUNT_SECRET";
	private static final String FALLBACK_SECRET_ENV = "APP_MASTER_KEY";
	/** Secret utilisé pour le compte de démonstration (chargé via variable d'environnement). */
	public static final String TEST_ACCOUNT_SECRET = resolveTestSecret();

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	/**
	 * Injecte le dépôt nécessaire pour tester l’existence du compte démo et le créer au besoin.
	 *
	 * @param userRepository accès Spring Data à la table {@code utilisateurs}
	 */
	public TestAccountInitializer(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Hook Spring Boot exécuté après levée du contexte : tente la création du compte démo une seule fois.
	 *
	 * @param args arguments CLI Spring Boot (non utilisés ici ; réservés aux extensions futures)
	 * @return aucune valeur ; journalise les skips ou erreurs de migration sans faire échouer le démarrage
	 */
	@Override
	public void run(String... args) {
		try {
			if (userRepository.existsByEmail(TEST_EMAIL)) {
				return;
			}
			if (TEST_ACCOUNT_SECRET == null || TEST_ACCOUNT_SECRET.isBlank()) {
				log.warn(
						"Compte démo non créé: aucune variable d'environnement {} ou {} n'est définie.",
						TEST_ACCOUNT_SECRET_ENV,
						FALLBACK_SECRET_ENV);
				return;
			}
			User user = new User();
			user.setEmail(TEST_EMAIL);
			user.setMotDePasse(passwordEncoder.encode(TEST_ACCOUNT_SECRET));
			user.setNom("Test");
			user.setPrenom("Toto");
			user.setRole("participant");
			userRepository.save(user);
		} catch (DataAccessException e) {
			log.warn(
					"Compte démo non initialisé (table utilisateurs absente ou migrations Laravel non appliquées). "
							+ "Après `php artisan migrate`, redémarre le service auth si besoin. Cause : {}",
					e.getMessage());
		}
	}

	private static String resolveTestSecret() {
		String secret = System.getenv(TEST_ACCOUNT_SECRET_ENV);
		if (secret != null && !secret.isBlank()) {
			return secret;
		}
		String fallback = System.getenv(FALLBACK_SECRET_ENV);
		return (fallback == null) ? "" : fallback;
	}
}
