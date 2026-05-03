package com.example.identity.application.usecase;

import com.example.identity.application.port.in.GetCurrentUserUseCase;
import com.example.identity.application.port.out.UserRepository;
import com.example.identity.domain.exception.UserNotFoundException;
import com.example.identity.domain.model.User;
import java.util.UUID;

public class GetCurrentUserService implements GetCurrentUserUseCase {

  private final UserRepository userRepository;

  public GetCurrentUserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public User getById(UUID userId) {
    return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
  }
}
