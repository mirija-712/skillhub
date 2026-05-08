package com.example.authentification_back.validation;

import com.example.authentification_back.exception.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyValidatorTest {

	private PasswordPolicyValidator validator;

	@BeforeEach
	void setUp() {
		validator = new PasswordPolicyValidator();
	}

	@Test
	void rejects_too_short() {
		assertThatThrownBy(() -> validator.assertCompliant("Aa1!short"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("12");
	}

	@Test
	void rejects_without_uppercase() {
		assertThatThrownBy(() -> validator.assertCompliant("aa1!aaaaaaaa"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("majuscule");
	}

	@Test
	void rejects_without_lowercase() {
		assertThatThrownBy(() -> validator.assertCompliant("AA1!AAAAAAAA"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("minuscule");
	}

	@Test
	void rejects_without_digit() {
		assertThatThrownBy(() -> validator.assertCompliant("Aa!!aaaaaaaa"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("chiffre");
	}

	@Test
	void rejects_without_special_character() {
		assertThatThrownBy(() -> validator.assertCompliant("Aa1aaaaaaaaa"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("spécial");
	}

	@Test
	void accepts_compliant_password() {
		assertThatCode(() -> validator.assertCompliant("Aa1!aaaaaaaa")).doesNotThrowAnyException();
	}

	@Test
	void rejects_null() {
		assertThatThrownBy(() -> validator.assertCompliant(null)).isInstanceOf(InvalidInputException.class);
	}

	@Test
	void rejects_too_long_password() {
		String tooLong = "Aa1!" + "a".repeat(130);
		assertThatThrownBy(() -> validator.assertCompliant(tooLong))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("128");
	}

	@Test
	void rejects_common_passwords_even_if_complex() {
		assertThatThrownBy(() -> validator.assertCompliant("Aa1!password123XYZ"))
				.isInstanceOf(InvalidInputException.class)
				.hasMessageContaining("trop commun");
	}
}
