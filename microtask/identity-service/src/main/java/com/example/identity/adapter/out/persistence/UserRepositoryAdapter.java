package com.example.identity.adapter.out.persistence;

import com.example.identity.application.port.out.UserRepository;
import com.example.identity.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

  private final UserJpaRepository jpa;

  public UserRepositoryAdapter(UserJpaRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public User save(User user) {
    UserEntity entity =
        new UserEntity(
            user.getId(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getCreatedAt(),
            user.getUpdatedAt());
    return toDomain(jpa.save(entity));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpa.findByEmail(email).map(UserRepositoryAdapter::toDomain);
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpa.findById(id).map(UserRepositoryAdapter::toDomain);
  }

  private static User toDomain(UserEntity e) {
    return new User(
        e.getId(), e.getEmail(), e.getPasswordHash(), e.getCreatedAt(), e.getUpdatedAt());
  }
}
