package com.taska.domain.task.controller;

import com.taska.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** HTTP representation of one persisted non-recurring task. */
public record NonRecurringTaskDto(
    TaskRepresentationKind kind,
    UUID id,
    String content,
    String description,
    UUID projectId,
    UUID parentId,
    Integer order,
    Integer priority,
    List<String> labels,
    Instant scheduledAt,
    Instant dueAt,
    Boolean allDay,
    Integer estimateMinutes,
    String mentionContext,
    Instant createdAt,
    Instant updatedAt,
    TaskType type,
    Boolean isCompleted,
    Instant completedAt)
    implements TaskDto {}
