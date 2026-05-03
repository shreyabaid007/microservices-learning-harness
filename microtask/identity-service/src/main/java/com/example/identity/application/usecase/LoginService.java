package com.example.identity.application.usecase;

import com.example.identity.application.port.in.LoginUseCase;
import com.example.identity.application.port.out.TokenIssuer;
import com.example.identity.application.port.out.UserRepository;
import com.example.identity.domain.exception.InvalidCredentialsException;
import com.example.identity.domain.model.User;
import com.example.identity.domain.service.PasswordHasher;

public class LoginService implements LoginUseCase {

  private final UserRepository userRepository;
  private final PasswordHasher passwordHasher;
  private final TokenIssuer tokenIssuer;

  public LoginService(
      UserRepository userRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
    this.userRepository = userRepository;
    this.passwordHasher = passwordHasher;
    this.tokenIssuer = tokenIssuer;
  }

  @Override
  public String login(String email, String password) {
    User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    if (!passwordHasher.verify(password, user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }
    return tokenIssuer.issue(user.getId());
  }
}
