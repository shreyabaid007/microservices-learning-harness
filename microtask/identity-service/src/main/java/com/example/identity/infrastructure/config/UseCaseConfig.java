package com.example.identity.infrastructure.config;

import com.example.identity.application.port.in.GetCurrentUserUseCase;
import com.example.identity.application.port.in.LoginUseCase;
import com.example.identity.application.port.in.RegisterUserUseCase;
import com.example.identity.application.port.out.TokenIssuer;
import com.example.identity.application.port.out.UserRepository;
import com.example.identity.application.usecase.GetCurrentUserService;
import com.example.identity.application.usecase.LoginService;
import com.example.identity.application.usecase.RegisterUserService;
import com.example.identity.domain.service.PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

  @Bean
  public RegisterUserUseCase registerUserUseCase(UserRepository repository, PasswordHasher hasher) {
    return new RegisterUserService(repository, hasher);
  }

  @Bean
  public LoginUseCase loginUseCase(
      UserRepository repository, PasswordHasher hasher, TokenIssuer issuer) {
    return new LoginService(repository, hasher, issuer);
  }

  @Bean
  public GetCurrentUserUseCase getCurrentUserUseCase(UserRepository repository) {
    return new GetCurrentUserService(repository);
  }
}
