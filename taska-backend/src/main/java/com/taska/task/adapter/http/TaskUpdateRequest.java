package com.taska.task.adapter.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taska.task.model.TaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Complete mutable representation used to replace a base task or recurring-series definition.
 */
public record TaskUpdateRequest(@JsonProperty(required = true) @NotBlank String content, @JsonProperty(required = true) @NotNull TaskType type,
        @JsonProperty(required = true) String description, @JsonProperty(required = true) UUID projectId,
        @JsonProperty(required = true) UUID parentId, @JsonProperty(required = true) @NotNull Integer order,
        @JsonProperty(required = true) @Min(1) @Max(4) Integer priority, @JsonProperty(required = true) @NotNull List<String> labels,
        @JsonProperty(required = true) Instant scheduledAt, @JsonProperty(required = true) Instant dueAt,
        @JsonProperty(required = true) @NotNull Boolean allDay, @JsonProperty(required = true) @NotNull Boolean isRecurring,
        @JsonProperty(required = true) Integer estimateMinutes, @JsonProperty(required = true) String mentionContext,
        @JsonProperty(required = true) String recurrenceRule) {
}
