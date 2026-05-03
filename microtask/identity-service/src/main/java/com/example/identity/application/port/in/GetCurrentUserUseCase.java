package com.example.identity.application.port.in;

import com.example.identity.domain.model.User;
import java.util.UUID;

public interface GetCurrentUserUseCase {

  User getById(UUID userId);
}
