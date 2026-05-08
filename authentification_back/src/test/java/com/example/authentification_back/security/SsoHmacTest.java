package com.example.authentification_back.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SsoHmacTest {

	@Test
	void message_to_sign_has_expected_format() {
		String message = SsoHmac.messageToSign("user@example.com", "nonce-123", 1710000000L);
		assertThat(message).isEqualTo("user@example.com:nonce-123:1710000000");
	}

	@Test
	void hmac_sha256_hex_is_deterministic() {
		String a = SsoHmac.hmacSha256Hex("secret", "payload");
		String b = SsoHmac.hmacSha256Hex("secret", "payload");
		assertThat(a).isEqualTo(b);
		assertThat(a).hasSize(64);
	}

	@Test
	void constant_time_equals_hex_returns_true_for_same_hex() {
		String hex = SsoHmac.hmacSha256Hex("secret", "payload");
		assertThat(SsoHmac.constantTimeEqualsHex(hex, hex)).isTrue();
	}

	@Test
	void constant_time_equals_hex_returns_false_for_null_values() {
		String hex = SsoHmac.hmacSha256Hex("secret", "payload");
		assertThat(SsoHmac.constantTimeEqualsHex(null, hex)).isFalse();
		assertThat(SsoHmac.constantTimeEqualsHex(hex, null)).isFalse();
	}

	@Test
	void constant_time_equals_hex_returns_false_for_different_lengths() {
		assertThat(SsoHmac.constantTimeEqualsHex("aabb", "aa")).isFalse();
	}

	@Test
	void constant_time_equals_hex_returns_false_for_invalid_hex_strings() {
		assertThat(SsoHmac.constantTimeEqualsHex("zzzz", "zzzz")).isFalse();
	}
}
