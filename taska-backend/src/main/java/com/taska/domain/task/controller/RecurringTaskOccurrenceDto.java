package com.taska.domain.task.controller;

import com.taska.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** HTTP representation of one expanded recurring occurrence. */
public record RecurringTaskOccurrenceDto(
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
    String recurrenceRule,
    Instant rruleEndsAt,
    Boolean isCompleted,
    Instant completedAt,
    UUID instanceId,
    Instant occurrenceScheduledAt,
    Boolean isVirtual)
    implements TaskDto {}
