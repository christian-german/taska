package com.taska.domain.task.service;

import com.taska.domain.task.TaskType;
import com.taska.domain.task.occurrence.RecurrenceScope;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Transport-independent optional changes accepted by the existing MCP update tool. */
public record TaskPatchParameters(
    String content,
    String description,
    UUID projectId,
    UUID parentId,
    Integer position,
    Integer priority,
    List<String> labels,
    Instant scheduledAt,
    Instant dueAt,
    Boolean allDay,
    Boolean recurring,
    Integer estimateMinutes,
    String mentionContext,
    String recurrenceRule,
    RecurrenceScope scope,
    Instant occurrenceScheduledAt,
    TaskType type) {}
