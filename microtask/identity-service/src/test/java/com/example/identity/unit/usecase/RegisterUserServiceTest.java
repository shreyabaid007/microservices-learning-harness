package com.example.identity.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.identity.application.port.out.UserRepository;
import com.example.identity.application.usecase.RegisterUserService;
import com.example.identity.domain.exception.EmailAlreadyExistsException;
import com.example.identity.domain.model.User;
import com.example.identity.domain.service.PasswordHasher;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordHasher passwordHasher;

  private RegisterUserService service;

  @BeforeEach
  void setUp() {
    service = new RegisterUserService(userRepository, passwordHasher);
  }

  @Test
  void registers_new_user_with_hashed_password() {
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
    when(passwordHasher.hash("plain")).thenReturn("$2a$10$hashed");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    User saved = service.register("user@example.com", "plain");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User persisted = captor.getValue();

    assertThat(persisted.getEmail()).isEqualTo("user@example.com");
    assertThat(persisted.getPasswordHash()).isEqualTo("$2a$10$hashed");
    assertThat(persisted.getId()).isNotNull();
    assertThat(persisted.getCreatedAt()).isNotNull();
    assertThat(saved).isSameAs(persisted);
  }

  @Test
  void rejects_duplicate_email_with_email_already_exists_exception() {
    User existing =
        new User(
            java.util.UUID.randomUUID(),
            "user@example.com",
            "$2a$10$existing",
            java.time.Instant.now(),
            java.time.Instant.now());
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.register("user@example.com", "plain"))
        .isInstanceOf(EmailAlreadyExistsException.class);

    verify(userRepository, never()).save(any());
    verify(passwordHasher, never()).hash(any());
  }
}
