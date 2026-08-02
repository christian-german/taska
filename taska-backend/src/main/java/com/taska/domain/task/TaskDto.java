package com.taska.domain.task;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Transfer object representing a task returned by the API.
 * For recurring tasks each occurrence is represented as a separate {@code TaskDto}, with
 * occurrence-specific fields ({@code instanceId}, {@code occurrenceScheduledAt}, {@code isVirtual})
 * populated to distinguish it from a plain task.
 *
 * @param id              unique identifier of the task
 * @param content         title / main text of the task
 * @param description     optional longer description
 * @param projectId       project this task belongs to; {@code null} for subtasks without a project
 * @param sectionId       section within the project; {@code null} when the task is unsectioned
 * @param parentId        parent task UUID for subtasks; {@code null} for top-level tasks
 * @param order           display position within its container (maps to the entity's {@code position})
 * @param priority        optional manual urgency level: 1 = urgent, 2 = high, 3 = medium, 4 = normal
 * @param labels          list of label names attached to the task
 * @param isCompleted     whether the task has been completed
 * @param scheduledAt     planned schedule time in UTC; {@code null} when the task is unscheduled
 * @param allDay          when {@code true} the scheduled time has no specific time component
 * @param isRecurring     whether the task repeats according to a recurrence rule
 * @param estimateMinutes optional time estimate in minutes
 * @param mentionContext  raw context string used by clients when the task was created via @-mention
 * @param recurrenceRule  iCal4j RRULE string describing the recurrence pattern (e.g. {@code "FREQ=DAILY"})
 * @param createdAt       timestamp when the task entity was first persisted
 * @param updatedAt       timestamp of the last update to the task entity
 * @param completedAt     timestamp when the task was completed; {@code null} if still open
 * @param instanceId      UUID of the persisted {@code TaskInstance} for this occurrence;
 *                        {@code null} when the occurrence is virtual (not yet modified or completed)
 * @param occurrenceScheduledAt for recurring tasks, the exact UTC instant this occurrence falls on;
 *                        distinct from the task's mutable {@code scheduledAt}
 * @param isVirtual       {@code true} when no {@code TaskInstance} exists for this occurrence
 *                        (it has never been completed, skipped, or modified)
 * @param rruleEndsAt     the instant at which the recurrence series is truncated; occurrences
 *                        at or after this instant are not generated
 */
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
        Instant scheduledAt,
        Boolean allDay,
        Boolean isRecurring,
        Integer estimateMinutes,
        String mentionContext,
        String recurrenceRule,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        UUID instanceId,
        Instant occurrenceScheduledAt,
        Boolean isVirtual,
        Instant rruleEndsAt,
        TaskType type
) {
    /** Compatibility constructor for callers compiled before task type was introduced. */
    public TaskDto(UUID id, String content, String description, UUID projectId, UUID sectionId,
                   UUID parentId, Integer order, Integer priority, List<String> labels,
                   Boolean isCompleted, Instant scheduledAt, Boolean allDay, Boolean isRecurring,
                   Integer estimateMinutes, String mentionContext, String recurrenceRule,
                   Instant createdAt, Instant updatedAt, Instant completedAt, UUID instanceId,
                   Instant occurrenceScheduledAt, Boolean isVirtual, Instant rruleEndsAt) {
        this(id, content, description, projectId, sectionId, parentId, order, priority, labels,
                isCompleted, scheduledAt, allDay, isRecurring, estimateMinutes, mentionContext,
                recurrenceRule, createdAt, updatedAt, completedAt, instanceId, occurrenceScheduledAt,
                isVirtual, rruleEndsAt, TaskType.TODO);
    }
}
