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
 * (only non-null fields are applied via {@code applyPatch}). A task's planned time is
 * {@code scheduledAt}; {@code occurrenceScheduledAt} is separate and only selects a generated
 * recurring occurrence for a scoped update.
 * <p>
 * For recurring task updates, {@code scope} and {@code occurrenceScheduledAt} control which
 * occurrences are affected.
 *
 * @param content         required task title; must not be blank
 * @param description     optional longer description; {@code null} leaves the existing value unchanged on update
 * @param projectId       project to assign the task to; when {@code null} on create and no {@code parentId}
 *                        is set, the task is placed in the inbox project
 * @param sectionId       section within the project; {@code null} places the task outside any section
 * @param parentId        parent task UUID to create this as a subtask
 * @param order           display position within its container; defaults to 0 on create
 * @param priority        optional urgency level 1–4 (1 = urgent, 4 = normal); {@code null} means
 *                        no manual priority is assigned; supplied values must be between 1 and 4
 * @param labels          list of label names to attach; replaces the existing label list on update
 * @param scheduledAt     planned schedule time in UTC; {@code null} means the task is unscheduled
 * @param dueAt           deadline in UTC; {@code null} means the task has no deadline
 * @param allDay          when {@code true} the scheduled time has no specific time component; defaults to {@code false}
 * @param isRecurring     whether the task repeats; defaults to {@code false} on create
 * @param estimateMinutes optional time estimate in minutes; must be positive
 * @param mentionContext  raw context string captured when the task is created via @-mention
 * @param recurrenceRule  iCal4j RRULE string or shorthand alias ("daily", "weekly", "monthly", "yearly");
 *                        aliases are normalised to their RRULE equivalents on save
 * @param scope           for recurring task updates: {@code THIS_ONLY} modifies only the specified
 *                        occurrence; {@code FROM_THIS} truncates the series and creates a new one
 *                        from {@code occurrenceScheduledAt} onwards; {@code null} treats the task as non-recurring
 * @param occurrenceScheduledAt for recurring task updates, identifies the specific generated occurrence to act on;
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
        Instant scheduledAt,
        Instant dueAt,
        Boolean allDay,
        Boolean isRecurring,
        @Positive
        Integer estimateMinutes,
        String mentionContext,
        String recurrenceRule,
        RecurrenceScope scope,
        Instant occurrenceScheduledAt,
        TaskType type
) {
    /** Compatibility constructor for clients and tests that omit the optional task type. */
    public TaskRequest(String content, String description, UUID projectId, UUID sectionId,
                       UUID parentId, Integer order, Integer priority, List<String> labels,
                       Instant scheduledAt, Boolean allDay, Boolean isRecurring, Integer estimateMinutes,
                       String mentionContext, String recurrenceRule, RecurrenceScope scope,
                       Instant occurrenceScheduledAt) {
        this(content, description, projectId, sectionId, parentId, order, priority, labels, scheduledAt, null,
                allDay, isRecurring, estimateMinutes, mentionContext, recurrenceRule, scope,
                occurrenceScheduledAt, null);
    }

    /** Compatibility constructor for callers that provide a task type but no due date. */
    public TaskRequest(String content, String description, UUID projectId, UUID sectionId,
                       UUID parentId, Integer order, Integer priority, List<String> labels,
                       Instant scheduledAt, Boolean allDay, Boolean isRecurring, Integer estimateMinutes,
                       String mentionContext, String recurrenceRule, RecurrenceScope scope,
                       Instant occurrenceScheduledAt, TaskType type) {
        this(content, description, projectId, sectionId, parentId, order, priority, labels, scheduledAt, null,
                allDay, isRecurring, estimateMinutes, mentionContext, recurrenceRule, scope,
                occurrenceScheduledAt, type);
    }
}
