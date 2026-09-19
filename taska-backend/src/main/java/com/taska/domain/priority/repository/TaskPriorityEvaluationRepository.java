package com.taska.domain.priority.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskPriorityEvaluationRepository
    extends JpaRepository<TaskPriorityEvaluation, UUID> {

  Optional<TaskPriorityEvaluation> findByTaskId(UUID taskId);

  void deleteByTaskId(UUID taskId);
}
