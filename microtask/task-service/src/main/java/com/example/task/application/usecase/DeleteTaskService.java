package com.example.task.application.usecase;

import com.example.task.application.port.in.DeleteTaskUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.domain.exception.TaskNotFoundException;
import java.util.UUID;

public class DeleteTaskService implements DeleteTaskUseCase {

  private final TaskRepository taskRepository;

  public DeleteTaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public void delete(UUID id, UUID userId) {
    taskRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new TaskNotFoundException(id));
    taskRepository.deleteById(id);
  }
}
