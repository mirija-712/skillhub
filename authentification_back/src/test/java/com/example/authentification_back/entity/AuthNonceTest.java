package com.example.authentification_back.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuthNonceTest {

	@Test
	void getters_and_setters_should_work() {
		AuthNonce nonce = new AuthNonce();
		Instant now = Instant.now();

		nonce.setId(1L);
		nonce.setUserId(8L);
		nonce.setNonce("nonce-value");
		nonce.setExpiresAt(now.plusSeconds(120));
		nonce.setConsumed(true);
		nonce.setCreatedAt(now);

		assertThat(nonce.getId()).isEqualTo(1L);
		assertThat(nonce.getUserId()).isEqualTo(8L);
		assertThat(nonce.getNonce()).isEqualTo("nonce-value");
		assertThat(nonce.getExpiresAt()).isEqualTo(now.plusSeconds(120));
		assertThat(nonce.isConsumed()).isTrue();
		assertThat(nonce.getCreatedAt()).isEqualTo(now);
	}

	@Test
	void pre_persist_sets_created_at_if_missing() throws Exception {
		AuthNonce nonce = new AuthNonce();
		Method prePersist = AuthNonce.class.getDeclaredMethod("prePersist");
		prePersist.setAccessible(true);
		prePersist.invoke(nonce);

		assertThat(nonce.getCreatedAt()).isNotNull();
	}

	@Test
	void pre_persist_keeps_existing_created_at() throws Exception {
		AuthNonce nonce = new AuthNonce();
		Instant created = Instant.parse("2026-01-01T00:00:00Z");
		nonce.setCreatedAt(created);

		Method prePersist = AuthNonce.class.getDeclaredMethod("prePersist");
		prePersist.setAccessible(true);
		prePersist.invoke(nonce);

		assertThat(nonce.getCreatedAt()).isEqualTo(created);
	}
}
