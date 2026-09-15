package com.taska.domain.priority.controller;

import com.taska.domain.priority.TaskPriorityEvaluation;
import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

public record TaskPriorityEvaluationDto(
    UUID taskId, int score, JsonNode components, Instant computedAt) {

  public static TaskPriorityEvaluationDto from(TaskPriorityEvaluation evaluation) {
    return new TaskPriorityEvaluationDto(
        evaluation.getTaskId(),
        evaluation.getScore(),
        evaluation.getComponents(),
        evaluation.getComputedAt());
  }
}
