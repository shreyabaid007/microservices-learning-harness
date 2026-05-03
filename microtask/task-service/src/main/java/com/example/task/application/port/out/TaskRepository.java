package com.example.task.application.port.out;

import com.example.task.domain.model.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {

  Task save(Task task);

  List<Task> findAllByUserId(UUID userId);

  Optional<Task> findByIdAndUserId(UUID id, UUID userId);

  void deleteById(UUID id);
}
