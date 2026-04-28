package com.taska.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TaskRequest(
        @NotBlank String content,
        String description,
        UUID projectId,
        UUID sectionId,
        UUID parentId,
        Integer order,
        Integer priority,
        List<String> labels,
        LocalDate dueDate,
        LocalDateTime dueDateTime,
        Boolean isRecurring,
        Integer estimateMinutes,
        String mentionContext,
        String recurrenceRule
) {}
