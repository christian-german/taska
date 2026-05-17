package com.taska.domain.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

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
        @Min(1) @Max(4)
        Integer priority,
        List<String> labels,
        LocalDateTime dueAt,
        Boolean allDay,
        Boolean isRecurring,
        @Positive
        Integer estimateMinutes,
        String mentionContext,
        String recurrenceRule
) {}
