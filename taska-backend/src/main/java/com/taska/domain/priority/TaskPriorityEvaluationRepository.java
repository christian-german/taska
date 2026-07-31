package com.taska.domain.priority;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskPriorityEvaluationRepository extends JpaRepository<TaskPriorityEvaluation, UUID> {

    Optional<TaskPriorityEvaluation> findByTaskId(UUID taskId);

    void deleteByTaskId(UUID taskId);
}
