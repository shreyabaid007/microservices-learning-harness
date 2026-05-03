package com.example.task.application.usecase;

import com.example.task.application.port.in.CreateTaskUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.domain.model.Task;
import java.time.Instant;
import java.util.UUID;

public class CreateTaskService implements CreateTaskUseCase {

  private final TaskRepository taskRepository;

  public CreateTaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public Task create(CreateTaskCommand command) {
    Instant now = Instant.now();
    Task task =
        new Task(
            UUID.randomUUID(),
            command.userId(),
            command.title(),
            command.description(),
            command.dueDate(),
            Boolean.TRUE.equals(command.isCompleted()),
            now,
            now);
    return taskRepository.save(task);
  }
}
