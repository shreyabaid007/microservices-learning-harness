package com.example.task.application.port.in;

import com.example.task.domain.model.Task;
import java.time.LocalDate;
import java.util.UUID;

public interface CreateTaskUseCase {

  Task create(CreateTaskCommand command);

  record CreateTaskCommand(
      UUID userId, String title, String description, LocalDate dueDate, Boolean isCompleted) {}
}
