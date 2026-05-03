package com.example.identity.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.identity.application.port.out.TokenIssuer;
import com.example.identity.application.port.out.UserRepository;
import com.example.identity.application.usecase.LoginService;
import com.example.identity.domain.exception.InvalidCredentialsException;
import com.example.identity.domain.model.User;
import com.example.identity.domain.service.PasswordHasher;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordHasher passwordHasher;
  @Mock private TokenIssuer tokenIssuer;

  private LoginService service;

  @BeforeEach
  void setUp() {
    service = new LoginService(userRepository, passwordHasher, tokenIssuer);
  }

  @Test
  void issues_token_on_valid_credentials() {
    UUID userId = UUID.randomUUID();
    User user = new User(userId, "user@example.com", "$2a$10$hashed", Instant.now(), Instant.now());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordHasher.verify("plain", "$2a$10$hashed")).thenReturn(true);
    when(tokenIssuer.issue(userId)).thenReturn("signed.jwt.token");

    String token = service.login("user@example.com", "plain");

    assertThat(token).isEqualTo("signed.jwt.token");
  }

  @Test
  void rejects_unknown_email_with_invalid_credentials() {
    when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login("ghost@example.com", "plain"))
        .isInstanceOf(InvalidCredentialsException.class);

    verify(tokenIssuer, never()).issue(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void rejects_wrong_password_with_invalid_credentials() {
    User user =
        new User(
            UUID.randomUUID(), "user@example.com", "$2a$10$hashed", Instant.now(), Instant.now());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordHasher.verify("wrong", "$2a$10$hashed")).thenReturn(false);

    assertThatThrownBy(() -> service.login("user@example.com", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class);

    verify(tokenIssuer, never()).issue(org.mockito.ArgumentMatchers.any());
  }
}
