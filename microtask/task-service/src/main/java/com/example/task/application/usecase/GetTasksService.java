package com.example.task.application.usecase;

import com.example.task.application.port.in.GetTasksUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.domain.model.Task;
import java.util.List;
import java.util.UUID;

public class GetTasksService implements GetTasksUseCase {

  private final TaskRepository taskRepository;

  public GetTasksService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public List<Task> getForUser(UUID userId) {
    return taskRepository.findAllByUserId(userId);
  }
}
