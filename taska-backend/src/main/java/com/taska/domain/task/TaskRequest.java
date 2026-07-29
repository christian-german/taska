package com.taska.domain.task;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating or updating a task.
 * All fields except {@code content} are optional; null values are ignored on update
 * (only non-null fields are applied via {@code applyPatch}).
 * <p>
 * For recurring task updates, {@code scope} and {@code scheduledAt} control which
 * occurrences are affected.
 *
 * @param content         required task title; must not be blank
 * @param description     optional longer description; {@code null} leaves the existing value unchanged on update
 * @param projectId       project to assign the task to; when {@code null} on create and no {@code parentId}
 *                        is set, the task is placed in the inbox project
 * @param sectionId       section within the project; {@code null} places the task outside any section
 * @param parentId        parent task UUID to create this as a subtask
 * @param order           display position within its container; defaults to 0 on create
 * @param priority        urgency level 1–4 (1 = urgent, 4 = normal); defaults to 4 on create;
 *                        must be between 1 and 4
 * @param labels          list of label names to attach; replaces the existing label list on update
 * @param dueAt           due date/time in UTC; {@code null} means no due date
 * @param allDay          when {@code true} the due date has no specific time component; defaults to {@code false}
 * @param isRecurring     whether the task repeats; defaults to {@code false} on create
 * @param estimateMinutes optional time estimate in minutes; must be positive
 * @param mentionContext  raw context string captured when the task is created via @-mention
 * @param recurrenceRule  iCal4j RRULE string or shorthand alias ("daily", "weekly", "monthly", "yearly");
 *                        aliases are normalised to their RRULE equivalents on save
 * @param scope           for recurring task updates: {@code THIS_ONLY} modifies only the specified
 *                        occurrence; {@code FROM_THIS} truncates the series and creates a new one
 *                        from {@code scheduledAt} onwards; {@code null} treats the task as non-recurring
 * @param scheduledAt     for recurring task updates, identifies the specific occurrence to act on;
 *                        required when {@code scope} is set
 */
public record  TaskRequest(
        @NotBlank String content,
        String description,
        UUID projectId,
        UUID sectionId,
        UUID parentId,
        Integer order,
        @Min(1) @Max(4)
        Integer priority,
        List<String> labels,
        Instant dueAt,
        Boolean allDay,
        Boolean isRecurring,
        @Positive
        Integer estimateMinutes,
        String mentionContext,
        String recurrenceRule,
        RecurrenceScope scope,
        Instant scheduledAt
) {}
