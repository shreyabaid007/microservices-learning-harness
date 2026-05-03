package com.example.task.application.port.in;

import java.util.UUID;

public interface DeleteTaskUseCase {

  void delete(UUID id, UUID userId);
}
