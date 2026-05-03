package com.example.task.application.port.in;

import com.example.task.domain.model.Task;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateTaskUseCase {

  Task update(UpdateTaskCommand command);

  record UpdateTaskCommand(
      UUID id,
      UUID userId,
      String title,
      String description,
      LocalDate dueDate,
      Boolean isCompleted) {}
}
