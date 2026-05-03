package com.example.task.adapter.out.persistence;

import com.example.task.application.port.out.TaskRepository;
import com.example.task.domain.model.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TaskRepositoryAdapter implements TaskRepository {

  private final TaskJpaRepository jpa;

  public TaskRepositoryAdapter(TaskJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Task save(Task task) {
    TaskEntity entity =
        new TaskEntity(
            task.getId(),
            task.getUserId(),
            task.getTitle(),
            task.getDescription(),
            task.getDueDate(),
            task.isCompleted(),
            task.getCreatedAt(),
            task.getUpdatedAt());
    return toDomain(jpa.save(entity));
  }

  @Override
  public List<Task> findAllByUserId(UUID userId) {
    return jpa.findAllByUserId(userId).stream().map(TaskRepositoryAdapter::toDomain).toList();
  }

  @Override
  public Optional<Task> findByIdAndUserId(UUID id, UUID userId) {
    return jpa.findByIdAndUserId(id, userId).map(TaskRepositoryAdapter::toDomain);
  }

  @Override
  public void deleteById(UUID id) {
    jpa.deleteById(id);
  }

  private static Task toDomain(TaskEntity e) {
    return new Task(
        e.getId(),
        e.getUserId(),
        e.getTitle(),
        e.getDescription(),
        e.getDueDate(),
        e.isCompleted(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
