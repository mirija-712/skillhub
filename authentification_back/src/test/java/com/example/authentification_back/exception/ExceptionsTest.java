package com.example.authentification_back.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionsTest {

	@Test
	void invalid_input_exception_should_keep_message() {
		InvalidInputException ex = new InvalidInputException("invalid");
		assertThat(ex.getMessage()).isEqualTo("invalid");
	}

	@Test
	void authentication_failed_exception_should_keep_message() {
		AuthenticationFailedException ex = new AuthenticationFailedException("auth failed");
		assertThat(ex.getMessage()).isEqualTo("auth failed");
	}

	@Test
	void account_locked_exception_should_keep_message() {
		AccountLockedException ex = new AccountLockedException("locked");
		assertThat(ex.getMessage()).isEqualTo("locked");
	}

	@Test
	void resource_conflict_exception_should_keep_message() {
		ResourceConflictException ex = new ResourceConflictException("conflict");
		assertThat(ex.getMessage()).isEqualTo("conflict");
	}
}
