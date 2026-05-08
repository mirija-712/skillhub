package com.example.authentification_back.exception;

import com.example.authentification_back.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	private static HttpServletRequest mockRequest(String uri) {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		when(req.getRequestURI()).thenReturn(uri);
		return req;
	}

	@Test
	void handle_invalid_should_return_400_with_body() {
		ResponseEntity<ApiErrorResponse> response = handler.handleInvalid(
				new InvalidInputException("bad input"),
				mockRequest("/api/auth/register"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(400);
		assertThat(response.getBody().message()).isEqualTo("bad input");
		assertThat(response.getBody().path()).isEqualTo("/api/auth/register");
	}

	@Test
	void handle_locked_should_return_423() {
		ResponseEntity<ApiErrorResponse> response = handler.handleLocked(
				new AccountLockedException("locked"),
				mockRequest("/api/auth/login"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(423);
	}

	@Test
	void handle_auth_should_return_401() {
		ResponseEntity<ApiErrorResponse> response = handler.handleAuth(
				new AuthenticationFailedException("unauthorized"),
				mockRequest("/api/auth/me"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(401);
	}

	@Test
	void handle_conflict_should_return_409() {
		ResponseEntity<ApiErrorResponse> response = handler.handleConflict(
				new ResourceConflictException("email exists"),
				mockRequest("/api/auth/register"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().status()).isEqualTo(409);
	}

	@Test
	void handle_validation_should_join_field_errors() {
		MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
		BindingResult br = Mockito.mock(BindingResult.class);
		when(ex.getBindingResult()).thenReturn(br);
		when(br.getFieldErrors()).thenReturn(List.of(
				new FieldError("obj", "email", "invalide"),
				new FieldError("obj", "note", "doit etre entre 1 et 5")
		));

		ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, mockRequest("/api/test"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().message()).contains("email: invalide");
		assertThat(response.getBody().message()).contains("note: doit etre entre 1 et 5");
	}

	@Test
	void handle_validation_should_use_default_message_when_no_errors() {
		MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
		BindingResult br = Mockito.mock(BindingResult.class);
		when(ex.getBindingResult()).thenReturn(br);
		when(br.getFieldErrors()).thenReturn(List.of());

		ResponseEntity<ApiErrorResponse> response = handler.handleValidation(ex, mockRequest("/api/test"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().message()).isEqualTo("Données invalides");
	}
}
