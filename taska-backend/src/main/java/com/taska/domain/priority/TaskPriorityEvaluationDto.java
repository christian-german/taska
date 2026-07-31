package com.taska.domain.priority;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.UUID;

public record TaskPriorityEvaluationDto(UUID taskId, int score, JsonNode components, Instant computedAt) {

    static TaskPriorityEvaluationDto from(TaskPriorityEvaluation evaluation) {
        return new TaskPriorityEvaluationDto(evaluation.getTaskId(), evaluation.getScore(),
                evaluation.getComponents(), evaluation.getComputedAt());
    }
}
