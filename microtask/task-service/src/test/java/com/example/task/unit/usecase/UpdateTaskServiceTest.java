package com.example.task.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.task.application.port.in.UpdateTaskUseCase;
import com.example.task.application.port.in.UpdateTaskUseCase.UpdateTaskCommand;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.application.usecase.UpdateTaskService;
import com.example.task.domain.exception.TaskNotFoundException;
import com.example.task.domain.model.Task;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateTaskServiceTest {

  @Mock private TaskRepository taskRepository;

  private UpdateTaskService service;

  @BeforeEach
  void setUp() {
    service = new UpdateTaskService(taskRepository);
  }

  @Test
  void updates_task_owned_by_user_replacing_all_fields() {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant created = Instant.parse("2026-01-01T00:00:00Z");
    Task existing =
        new Task(id, userId, "Old", "old desc", LocalDate.of(2026, 1, 1), false, created, created);
    when(taskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(existing));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    Task result =
        service.update(
            new UpdateTaskCommand(
                id, userId, "New", "new desc", LocalDate.of(2026, 6, 15), Boolean.TRUE));

    ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).save(captor.capture());
    Task saved = captor.getValue();

    assertThat(saved.getId()).isEqualTo(id);
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getTitle()).isEqualTo("New");
    assertThat(saved.getDescription()).isEqualTo("new desc");
    assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 15));
    assertThat(saved.isCompleted()).isTrue();
    assertThat(saved.getCreatedAt()).isEqualTo(created);
    assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(created);
    assertThat(result).isSameAs(saved);
  }

  @Test
  void clears_optional_fields_when_command_passes_null() {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Instant created = Instant.parse("2026-01-01T00:00:00Z");
    Task existing =
        new Task(id, userId, "Old", "old desc", LocalDate.of(2026, 1, 1), true, created, created);
    when(taskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.of(existing));
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    service.update(new UpdateTaskCommand(id, userId, "New", null, null, Boolean.FALSE));

    ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).save(captor.capture());
    Task saved = captor.getValue();

    assertThat(saved.getDescription()).isNull();
    assertThat(saved.getDueDate()).isNull();
    assertThat(saved.isCompleted()).isFalse();
  }

  @Test
  void throws_TaskNotFoundException_when_repository_returns_empty() {
    UUID id = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    when(taskRepository.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.update(new UpdateTaskCommand(id, userId, "T", null, null, false)))
        .isInstanceOf(TaskNotFoundException.class);

    verify(taskRepository, never()).save(any());
  }

  @Test
  void empty_optional_covers_both_missing_and_unowned_no_separate_check() {
    UUID id = UUID.randomUUID();
    UUID otherUser = UUID.randomUUID();
    when(taskRepository.findByIdAndUserId(id, otherUser)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.update(new UpdateTaskCommand(id, otherUser, "T", null, null, false)))
        .isInstanceOf(TaskNotFoundException.class);
  }

  @Test
  void implements_UpdateTaskUseCase() {
    assertThat(service).isInstanceOf(UpdateTaskUseCase.class);
  }
}
