package com.example.identity.application.port.in;

import com.example.identity.domain.model.User;

public interface RegisterUserUseCase {

  User register(String email, String password);
}
