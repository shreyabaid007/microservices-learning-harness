package com.example.task.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.task.application.port.in.GetTasksUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.application.usecase.GetTasksService;
import com.example.task.domain.model.Task;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTasksServiceTest {

  @Mock private TaskRepository taskRepository;

  private GetTasksService service;

  @BeforeEach
  void setUp() {
    service = new GetTasksService(taskRepository);
  }

  @Test
  void returns_tasks_for_user_from_repository() {
    UUID userId = UUID.randomUUID();
    Task t1 = task(userId, "A");
    Task t2 = task(userId, "B");
    when(taskRepository.findAllByUserId(userId)).thenReturn(List.of(t1, t2));

    List<Task> result = service.getForUser(userId);

    assertThat(result).containsExactly(t1, t2);
  }

  @Test
  void returns_empty_list_when_user_has_no_tasks() {
    UUID userId = UUID.randomUUID();
    when(taskRepository.findAllByUserId(userId)).thenReturn(List.of());

    assertThat(service.getForUser(userId)).isEmpty();
  }

  @Test
  void implements_GetTasksUseCase() {
    assertThat(service).isInstanceOf(GetTasksUseCase.class);
  }

  private static Task task(UUID userId, String title) {
    Instant now = Instant.now();
    return new Task(UUID.randomUUID(), userId, title, null, null, false, now, now);
  }
}
