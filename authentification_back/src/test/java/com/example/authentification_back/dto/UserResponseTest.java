package com.example.authentification_back.dto;

import com.example.authentification_back.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

	@Test
	void profile_should_map_user_without_token() {
		User user = new User();
		Instant created = Instant.parse("2026-01-01T00:00:00Z");
		user.setId(9L);
		user.setEmail("user@example.com");
		user.setNom("Nom");
		user.setPrenom("Prenom");
		user.setRole("participant");
		user.setCreatedAt(created);

		UserResponse response = UserResponse.profile(user);

		assertThat(response.id()).isEqualTo(9L);
		assertThat(response.email()).isEqualTo("user@example.com");
		assertThat(response.nom()).isEqualTo("Nom");
		assertThat(response.prenom()).isEqualTo("Prenom");
		assertThat(response.role()).isEqualTo("participant");
		assertThat(response.createdAt()).isEqualTo(created);
		assertThat(response.token()).isNull();
	}

	@Test
	void login_should_map_user_with_token() {
		User user = new User();
		Instant created = Instant.parse("2026-01-01T00:00:00Z");
		user.setId(7L);
		user.setEmail("login@example.com");
		user.setNom("Login");
		user.setPrenom("User");
		user.setRole("formateur");
		user.setCreatedAt(created);

		UserResponse response = UserResponse.login(user, "token-xyz");

		assertThat(response.id()).isEqualTo(7L);
		assertThat(response.email()).isEqualTo("login@example.com");
		assertThat(response.role()).isEqualTo("formateur");
		assertThat(response.createdAt()).isEqualTo(created);
		assertThat(response.token()).isEqualTo("token-xyz");
	}
}
