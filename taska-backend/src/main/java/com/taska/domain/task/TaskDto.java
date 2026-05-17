package com.taska.domain.task;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TaskDto(
        UUID id,
        String content,
        String description,
        UUID projectId,
        UUID sectionId,
        UUID parentId,
        Integer order,
        Integer priority,
        List<String> labels,
        Boolean isCompleted,
        Instant dueAt,
        Boolean allDay,
        Boolean isRecurring,
        Integer estimateMinutes,
        String mentionContext,
        String recurrenceRule,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {}
