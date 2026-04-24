package com.example.authentification_back.config;

import com.example.authentification_back.entity.User;
import com.example.authentification_back.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestAccountInitializerTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private TestAccountInitializer initializer;

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Test
	void shouldCreateDemoUserWhenEmailDoesNotExist() {
		when(userRepository.existsByEmail(TestAccountInitializer.TEST_EMAIL)).thenReturn(false);

		initializer.run();

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		User saved = captor.getValue();

		assertEquals(TestAccountInitializer.TEST_EMAIL, saved.getEmail());
		assertEquals("Test", saved.getNom());
		assertEquals("Toto", saved.getPrenom());
		assertEquals("participant", saved.getRole());
		assertTrue(encoder.matches(TestAccountInitializer.TEST_PASSWORD_PLAIN, saved.getMotDePasse()));
	}

	@Test
	void shouldDoNothingWhenDemoUserAlreadyExists() {
		when(userRepository.existsByEmail(TestAccountInitializer.TEST_EMAIL)).thenReturn(true);

		initializer.run();

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void shouldNotCrashWhenRepositoryThrowsDataAccessException() {
		when(userRepository.existsByEmail(TestAccountInitializer.TEST_EMAIL)).thenReturn(false);
		doThrow(new DataAccessResourceFailureException("db unavailable")).when(userRepository).save(any(User.class));

		initializer.run();

		verify(userRepository).save(any(User.class));
	}
}
