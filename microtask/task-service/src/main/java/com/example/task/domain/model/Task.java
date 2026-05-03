package com.example.task.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class Task {

  private final UUID id;
  private final UUID userId;
  private final String title;
  private final String description;
  private final LocalDate dueDate;
  private final boolean completed;
  private final Instant createdAt;
  private final Instant updatedAt;

  public Task(
      UUID id,
      UUID userId,
      String title,
      String description,
      LocalDate dueDate,
      boolean completed,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.title = Objects.requireNonNull(title, "title");
    this.description = description;
    this.dueDate = dueDate;
    this.completed = completed;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public boolean isCompleted() {
    return completed;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
