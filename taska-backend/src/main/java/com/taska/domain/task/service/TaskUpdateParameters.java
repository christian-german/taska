package com.taska.domain.task.service;

import com.taska.domain.task.TaskType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Application parameters for replacing every mutable property of a base task. */
public record TaskUpdateParameters(
    String content,
    TaskType type,
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
    String recurrenceRule) {}
