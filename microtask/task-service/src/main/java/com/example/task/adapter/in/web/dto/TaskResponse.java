package com.example.task.adapter.in.web.dto;

import com.example.task.domain.model.Task;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TaskResponse(
    UUID id,
    UUID userId,
    String title,
    String description,
    LocalDate dueDate,
    boolean isCompleted,
    Instant createdAt,
    Instant updatedAt) {

  public static TaskResponse from(Task task) {
    return new TaskResponse(
        task.getId(),
        task.getUserId(),
        task.getTitle(),
        task.getDescription(),
        task.getDueDate(),
        task.isCompleted(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }
}
