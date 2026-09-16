package com.taska.domain.priority.controller;

import java.time.Instant;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Transfer object representing the computed priority evaluation of a task.
 *
 * @param taskId task the evaluation was computed for
 * @param score computed priority score
 * @param components raw per-component breakdown behind the score
 * @param computedAt timestamp of the computation
 */
public record TaskPriorityEvaluationDto(
    UUID taskId, int score, JsonNode components, Instant computedAt) {}
