package com.example.task.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.task.application.port.in.DeleteTaskUseCase;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.application.usecase.DeleteTaskService;
import com.example.task.domain.exception.TaskNotFoundException;
import com.example.task.domain.model.Task;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteTaskServiceTest {

  @Mock private TaskRepository taskRepository;

  private DeleteTaskService service;

  @BeforeEach
  void setUp() {
    service = new DeleteTaskService(taskRepository);
  }

  @Test
  void deletes_task_owned_by_user() {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    Task existing = new Task(id, userId, "T", null, null, false, now, now);
    when(taskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(existing));

    service.delete(id, userId);

    verify(taskRepository).deleteById(id);
  }

  @Test
  void throws_TaskNotFoundException_when_repository_returns_empty() {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(taskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(id, userId)).isInstanceOf(TaskNotFoundException.class);

    verify(taskRepository, never()).deleteById(id);
  }

  @Test
  void empty_optional_for_unowned_task_throws_not_found_no_separate_check() {
    UUID id = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    when(taskRepository.findByIdAndUserId(id, otherUser)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(id, otherUser))
        .isInstanceOf(TaskNotFoundException.class);

    verify(taskRepository, never()).deleteById(id);
  }

  @Test
  void implements_DeleteTaskUseCase() {
    assertThat(service).isInstanceOf(DeleteTaskUseCase.class);
  }
}
