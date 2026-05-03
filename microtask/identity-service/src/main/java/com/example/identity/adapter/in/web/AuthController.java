package com.example.identity.adapter.in.web;

import com.example.identity.adapter.in.web.dto.LoginRequest;
import com.example.identity.adapter.in.web.dto.RegisterRequest;
import com.example.identity.adapter.in.web.dto.TokenResponse;
import com.example.identity.adapter.in.web.dto.UserResponse;
import com.example.identity.application.port.in.GetCurrentUserUseCase;
import com.example.identity.application.port.in.LoginUseCase;
import com.example.identity.application.port.in.RegisterUserUseCase;
import com.example.identity.domain.model.User;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final RegisterUserUseCase registerUserUseCase;
  private final LoginUseCase loginUseCase;
  private final GetCurrentUserUseCase getCurrentUserUseCase;

  public AuthController(
      RegisterUserUseCase registerUserUseCase,
      LoginUseCase loginUseCase,
      GetCurrentUserUseCase getCurrentUserUseCase) {
    this.registerUserUseCase = registerUserUseCase;
    this.loginUseCase = loginUseCase;
    this.getCurrentUserUseCase = getCurrentUserUseCase;
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    User user = registerUserUseCase.register(request.email(), request.password());
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
    String token = loginUseCase.login(request.email(), request.password());
    return ResponseEntity.ok(new TokenResponse(token));
  }

  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    User user = getCurrentUserUseCase.getById(userId);
    return ResponseEntity.ok(UserResponse.from(user));
  }
}
