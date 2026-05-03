package com.example.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

  private final UUID id;
  private final String email;
  private final String passwordHash;
  private final Instant createdAt;
  private final Instant updatedAt;

  public User(UUID id, String email, String passwordHash, Instant createdAt, Instant updatedAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.email = Objects.requireNonNull(email, "email");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
