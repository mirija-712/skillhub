package com.example.authentification_back.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AuthSecurityPropertiesTest {

	@Test
	void defaults_should_match_expected_values() {
		AuthSecurityProperties props = new AuthSecurityProperties();
		assertThat(props.getLockDuration()).isEqualTo(Duration.ofMinutes(2));
		assertThat(props.getMaxFailedAttempts()).isEqualTo(5);
		assertThat(props.getTimestampSkewSeconds()).isEqualTo(60);
		assertThat(props.getNonceTtlSeconds()).isEqualTo(120);
	}

	@Test
	void setters_should_update_values() {
		AuthSecurityProperties props = new AuthSecurityProperties();
		props.setLockDuration(Duration.ofSeconds(30));
		props.setMaxFailedAttempts(7);
		props.setTimestampSkewSeconds(90);
		props.setNonceTtlSeconds(240);

		assertThat(props.getLockDuration()).isEqualTo(Duration.ofSeconds(30));
		assertThat(props.getMaxFailedAttempts()).isEqualTo(7);
		assertThat(props.getTimestampSkewSeconds()).isEqualTo(90);
		assertThat(props.getNonceTtlSeconds()).isEqualTo(240);
	}
}
