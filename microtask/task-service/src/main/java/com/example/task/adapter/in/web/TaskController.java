package com.example.task.adapter.in.web;

import com.example.task.adapter.in.web.dto.CreateTaskRequest;
import com.example.task.adapter.in.web.dto.TaskResponse;
import com.example.task.adapter.in.web.dto.UpdateTaskRequest;
import com.example.task.application.port.in.CreateTaskUseCase;
import com.example.task.application.port.in.CreateTaskUseCase.CreateTaskCommand;
import com.example.task.application.port.in.DeleteTaskUseCase;
import com.example.task.application.port.in.GetTasksUseCase;
import com.example.task.application.port.in.UpdateTaskUseCase;
import com.example.task.application.port.in.UpdateTaskUseCase.UpdateTaskCommand;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  private final CreateTaskUseCase createTaskUseCase;
  private final GetTasksUseCase getTasksUseCase;
  private final UpdateTaskUseCase updateTaskUseCase;
  private final DeleteTaskUseCase deleteTaskUseCase;

  public TaskController(
      CreateTaskUseCase createTaskUseCase,
      GetTasksUseCase getTasksUseCase,
      UpdateTaskUseCase updateTaskUseCase,
      DeleteTaskUseCase deleteTaskUseCase) {
    this.createTaskUseCase = createTaskUseCase;
    this.getTasksUseCase = getTasksUseCase;
    this.updateTaskUseCase = updateTaskUseCase;
    this.deleteTaskUseCase = deleteTaskUseCase;
  }

  @PostMapping
  public ResponseEntity<TaskResponse> create(
      Authentication authentication, @Valid @RequestBody CreateTaskRequest request) {
    UUID userId = UUID.fromString(authentication.getName());
    var task =
        createTaskUseCase.create(
            new CreateTaskCommand(
                userId,
                request.title(),
                request.description(),
                request.dueDate(),
                request.isCompleted()));
    return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
  }

  @GetMapping
  public ResponseEntity<List<TaskResponse>> list(Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    List<TaskResponse> body =
        getTasksUseCase.getForUser(userId).stream().map(TaskResponse::from).toList();
    return ResponseEntity.ok(body);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TaskResponse> update(
      Authentication authentication,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateTaskRequest request) {
    UUID userId = UUID.fromString(authentication.getName());
    var task =
        updateTaskUseCase.update(
            new UpdateTaskCommand(
                id,
                userId,
                request.title(),
                request.description(),
                request.dueDate(),
                request.isCompleted()));
    return ResponseEntity.ok(TaskResponse.from(task));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID id) {
    UUID userId = UUID.fromString(authentication.getName());
    deleteTaskUseCase.delete(id, userId);
    return ResponseEntity.noContent().build();
  }
}
