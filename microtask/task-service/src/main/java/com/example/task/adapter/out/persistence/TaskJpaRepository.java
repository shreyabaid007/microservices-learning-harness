package com.example.task.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, UUID> {

  @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId")
  List<TaskEntity> findAllByUserId(UUID userId);

  @Query("SELECT t FROM TaskEntity t WHERE t.id = :id AND t.userId = :userId")
  Optional<TaskEntity> findByIdAndUserId(UUID id, UUID userId);
}
