package com.taska.domain.task.occurrence;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/** Mutable fields that a persisted recurring occurrence can override independently. */
public record OccurrenceUpdateRequest(
        @NotBlank String title, @Min(1) @Max(4) Integer priority,
        Instant scheduledAt, Instant dueAt
) {}
