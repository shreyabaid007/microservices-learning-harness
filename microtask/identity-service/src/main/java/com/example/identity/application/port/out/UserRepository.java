package com.example.identity.application.port.out;

import com.example.identity.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  User save(User user);

  Optional<User> findByEmail(String email);

  Optional<User> findById(UUID id);
}
