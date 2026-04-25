package com.taska.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TaskResponse(
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
        LocalDate dueDate,
        LocalDateTime dueDateTime,
        Boolean isRecurring,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {}
