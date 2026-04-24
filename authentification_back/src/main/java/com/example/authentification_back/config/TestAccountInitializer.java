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
 * Compte de démonstration (même email / mot de passe que les exemples du cours).
 */
@Component
public class TestAccountInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(TestAccountInitializer.class);

	public static final String TEST_EMAIL = "toto@example.com";
	private static final String TEST_PASSWORD_ENV = "TEST_ACCOUNT_PASSWORD";
	private static final String FALLBACK_PASSWORD_ENV = "APP_MASTER_KEY";
	public static final String TEST_PASSWORD_PLAIN = resolveTestPassword();

	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public TestAccountInitializer(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public void run(String... args) {
		try {
			if (userRepository.existsByEmail(TEST_EMAIL)) {
				return;
			}
			if (TEST_PASSWORD_PLAIN == null || TEST_PASSWORD_PLAIN.isBlank()) {
				log.warn(
						"Compte démo non créé: aucune variable d'environnement {} ou {} n'est définie.",
						TEST_PASSWORD_ENV,
						FALLBACK_PASSWORD_ENV);
				return;
			}
			User user = new User();
			user.setEmail(TEST_EMAIL);
			user.setMotDePasse(passwordEncoder.encode(TEST_PASSWORD_PLAIN));
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

	private static String resolveTestPassword() {
		String password = System.getenv(TEST_PASSWORD_ENV);
		if (password != null && !password.isBlank()) {
			return password;
		}
		String fallback = System.getenv(FALLBACK_PASSWORD_ENV);
		return (fallback == null) ? "" : fallback;
	}
}
