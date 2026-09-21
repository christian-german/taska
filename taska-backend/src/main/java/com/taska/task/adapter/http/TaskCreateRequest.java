package com.taska.task.adapter.http;

import com.taska.task.model.TaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Request payload containing only properties accepted when creating a task. */
public record TaskCreateRequest(@NotBlank String content, String description, UUID projectId, UUID parentId, Integer order,
        @Min(1) @Max(4) Integer priority, List<String> labels, Instant scheduledAt, Instant dueAt, Boolean allDay, Boolean isRecurring,
        @Positive Integer estimateMinutes, String mentionContext, String recurrenceRule, TaskType type) {
}
