package com.example.identity.adapter.in.web.dto;

import com.example.identity.domain.model.User;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, Instant createdAt) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getCreatedAt());
  }
}
