package com.taska.domain.task.service;

import com.taska.domain.task.definition.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Application parameters for creating a task. */
public record TaskCreateParameters(
    String content,
    String description,
    UUID projectId,
    UUID parentId,
    int position,
    Integer priority,
    List<String> labels,
    Instant scheduledAt,
    Instant dueAt,
    boolean allDay,
    boolean recurring,
    Integer estimateMinutes,
    String mentionContext,
    String recurrenceRule,
    TaskType type) {}
