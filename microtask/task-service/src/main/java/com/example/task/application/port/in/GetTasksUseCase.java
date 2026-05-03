package com.example.task.application.port.in;

import com.example.task.domain.model.Task;
import java.util.List;
import java.util.UUID;

public interface GetTasksUseCase {

  List<Task> getForUser(UUID userId);
}
