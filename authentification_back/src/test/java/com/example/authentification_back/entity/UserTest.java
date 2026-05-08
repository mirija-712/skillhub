package com.example.authentification_back.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

	@Test
	void getters_and_setters_should_work() {
		User user = new User();
		Instant now = Instant.now();

		user.setId(10L);
		user.setEmail("user@example.com");
		user.setMotDePasse("hashed");
		user.setNom("Diallo");
		user.setPrenom("Moussa");
		user.setRole("participant");
		user.setCreatedAt(now);
		user.setUpdatedAt(now);
		user.setToken("tok-123");
		user.setFailedLoginAttempts(3);
		user.setLockUntil(now.plusSeconds(60));

		assertThat(user.getId()).isEqualTo(10L);
		assertThat(user.getEmail()).isEqualTo("user@example.com");
		assertThat(user.getMotDePasse()).isEqualTo("hashed");
		assertThat(user.getNom()).isEqualTo("Diallo");
		assertThat(user.getPrenom()).isEqualTo("Moussa");
		assertThat(user.getRole()).isEqualTo("participant");
		assertThat(user.getCreatedAt()).isEqualTo(now);
		assertThat(user.getUpdatedAt()).isEqualTo(now);
		assertThat(user.getToken()).isEqualTo("tok-123");
		assertThat(user.getFailedLoginAttempts()).isEqualTo(3);
		assertThat(user.getLockUntil()).isEqualTo(now.plusSeconds(60));
	}

	@Test
	void pre_persist_should_initialize_missing_dates() throws Exception {
		User user = new User();
		Method prePersist = User.class.getDeclaredMethod("prePersist");
		prePersist.setAccessible(true);
		prePersist.invoke(user);

		assertThat(user.getCreatedAt()).isNotNull();
		assertThat(user.getUpdatedAt()).isNotNull();
	}

	@Test
	void pre_persist_should_keep_existing_created_at() throws Exception {
		User user = new User();
		Instant created = Instant.parse("2026-01-01T00:00:00Z");
		user.setCreatedAt(created);

		Method prePersist = User.class.getDeclaredMethod("prePersist");
		prePersist.setAccessible(true);
		prePersist.invoke(user);

		assertThat(user.getCreatedAt()).isEqualTo(created);
		assertThat(user.getUpdatedAt()).isNotNull();
	}

	@Test
	void pre_update_should_refresh_updated_at() throws Exception {
		User user = new User();
		user.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));

		Method preUpdate = User.class.getDeclaredMethod("preUpdate");
		preUpdate.setAccessible(true);
		preUpdate.invoke(user);

		assertThat(user.getUpdatedAt()).isAfter(Instant.parse("2026-01-01T00:00:00Z"));
	}
}
