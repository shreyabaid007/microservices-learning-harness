package com.example.task.infrastructure.config;

import com.example.task.application.port.in.CreateTaskUseCase;
import com.example.task.application.port.in.DeleteTaskUseCase;
import com.example.task.application.port.in.GetTasksUseCase;
import com.example.task.application.port.in.UpdateTaskUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.application.usecase.CreateTaskService;
import com.example.task.application.usecase.DeleteTaskService;
import com.example.task.application.usecase.GetTasksService;
import com.example.task.application.usecase.UpdateTaskService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

  @Bean
  public CreateTaskUseCase createTaskUseCase(TaskRepository repository) {
    return new CreateTaskService(repository);
  }

  @Bean
  public GetTasksUseCase getTasksUseCase(TaskRepository repository) {
    return new GetTasksService(repository);
  }

  @Bean
  public UpdateTaskUseCase updateTaskUseCase(TaskRepository repository) {
    return new UpdateTaskService(repository);
  }

  @Bean
  public DeleteTaskUseCase deleteTaskUseCase(TaskRepository repository) {
    return new DeleteTaskService(repository);
  }
}
