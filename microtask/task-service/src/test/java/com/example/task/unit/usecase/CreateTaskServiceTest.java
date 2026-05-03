package com.example.task.unit.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.task.application.port.in.CreateTaskUseCase;
import com.example.task.application.port.in.CreateTaskUseCase.CreateTaskCommand;
import com.example.task.application.port.out.TaskRepository;
import com.example.task.application.usecase.CreateTaskService;
import com.example.task.domain.model.Task;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTaskServiceTest {

  @Mock private TaskRepository taskRepository;

  private CreateTaskService service;

  @BeforeEach
  void setUp() {
    service = new CreateTaskService(taskRepository);
  }

  @Test
  void creates_task_with_provided_fields_and_userId_from_command() {
    UUID userId = UUID.randomUUID();
    LocalDate due = LocalDate.of(2026, 5, 1);
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    Task created =
        service.create(
            new CreateTaskCommand(userId, "Buy groceries", "Milk, eggs", due, Boolean.FALSE));

    ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
    verify(taskRepository).save(captor.capture());
    Task persisted = captor.getValue();

    assertThat(persisted.getId()).isNotNull();
    assertThat(persisted.getUserId()).isEqualTo(userId);
    assertThat(persisted.getTitle()).isEqualTo("Buy groceries");
    assertThat(persisted.getDescription()).isEqualTo("Milk, eggs");
    assertThat(persisted.getDueDate()).isEqualTo(due);
    assertThat(persisted.isCompleted()).isFalse();
    assertThat(persisted.getCreatedAt()).isNotNull();
    assertThat(persisted.getUpdatedAt()).isEqualTo(persisted.getCreatedAt());
    assertThat(created).isSameAs(persisted);
  }

  @Test
  void defaults_isCompleted_to_false_when_command_omits_it() {
    UUID userId = UUID.randomUUID();
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    Task created = service.create(new CreateTaskCommand(userId, "T", null, null, null));

    assertThat(created.isCompleted()).isFalse();
    assertThat(created.getDescription()).isNull();
    assertThat(created.getDueDate()).isNull();
  }

  @Test
  void honors_explicit_isCompleted_true() {
    UUID userId = UUID.randomUUID();
    when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

    Task created = service.create(new CreateTaskCommand(userId, "T", null, null, Boolean.TRUE));

    assertThat(created.isCompleted()).isTrue();
  }

  @Test
  void implements_CreateTaskUseCase() {
    assertThat(service).isInstanceOf(CreateTaskUseCase.class);
  }
}
