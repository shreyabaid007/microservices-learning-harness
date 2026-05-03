package com.example.task.application.usecase;

import com.example.task.application.port.in.UpdateTaskUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.domain.exception.TaskNotFoundException;
import com.example.task.domain.model.Task;
import java.time.Instant;

public class UpdateTaskService implements UpdateTaskUseCase {

  private final TaskRepository taskRepository;

  public UpdateTaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public Task update(UpdateTaskCommand command) {
    Task existing =
        taskRepository
            .findByIdAndUserId(command.id(), command.userId())
            .orElseThrow(() -> new TaskNotFoundException(command.id()));

    Task updated =
        new Task(
            existing.getId(),
            existing.getUserId(),
            command.title(),
            command.description(),
            command.dueDate(),
            Boolean.TRUE.equals(command.isCompleted()),
            existing.getCreatedAt(),
            Instant.now());
    return taskRepository.save(updated);
  }
}
