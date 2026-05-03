package com.example.identity.application.usecase;

import com.example.identity.application.port.in.RegisterUserUseCase;
import com.example.identity.application.port.out.UserRepository;
import com.example.identity.domain.exception.EmailAlreadyExistsException;
import com.example.identity.domain.model.User;
import com.example.identity.domain.service.PasswordHasher;
import java.time.Instant;
import java.util.UUID;

public class RegisterUserService implements RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;

  public RegisterUserService(UserRepository userRepository, PasswordHasher passwordHasher) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
  }

  @Override
  public User register(String email, String password) {
    if (userRepository.findByEmail(email).isPresent()) {
      throw new EmailAlreadyExistsException(email);
    }
    Instant now = Instant.now();
    User user = new User(UUID.randomUUID(), email, passwordHasher.hash(password), now, now);
    return userRepository.save(user);
  }
}
