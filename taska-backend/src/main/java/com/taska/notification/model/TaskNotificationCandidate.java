package com.taska.notification.model;

import com.taska.task.model.RecurringTaskOccurrenceResult;
import com.taska.task.model.Task;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Effective task-like values used to dispatch one scheduled notification.
 *
 * @param task backing task or recurring series
 * @param content effective notification title content
 * @param description notification body content
 * @param scheduledAt effective instant at which the candidate is scheduled
 * @param occurrenceScheduledAt stable recurring occurrence identity, or {@code null} for a non-recurring task
 */
public record TaskNotificationCandidate(Task task, String content, String description, Instant scheduledAt, Instant occurrenceScheduledAt) {

    public TaskNotificationCandidate {
        Objects.requireNonNull(task, "task");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(scheduledAt, "scheduledAt");
    }

    /**
     * Creates a candidate backed directly by a non-recurring task.
     *
     * @param task eligible non-recurring task
     * @return candidate exposing the task's persisted notification values
     * @throws IllegalArgumentException if the task is a recurring series
     */
    public static TaskNotificationCandidate nonRecurring(Task task) {
        if (Boolean.TRUE.equals(task.getIsRecurring())) {
            throw new IllegalArgumentException("A non-recurring notification candidate requires a task");
        }
        return new TaskNotificationCandidate(task, task.getContent(), task.getDescription(), task.getScheduledAt(), null);
    }

    /**
     * Creates a candidate from an occurrence after resolving its sparse overrides.
     *
     * @param occurrence resolved recurring occurrence
     * @return candidate exposing the occurrence's effective values
     */
    public static TaskNotificationCandidate recurring(RecurringTaskOccurrenceResult occurrence) {
        return new TaskNotificationCandidate(
                occurrence.task(),
                occurrence.resolvedContent(),
                occurrence.task().getDescription(),
                occurrence.resolvedScheduledAt(),
                occurrence.occurrenceScheduledAt());
    }

    /** Returns the identifier used in the Firebase payload. */
    public UUID taskId() {
        return task.getId();
    }

    /**
     * Returns whether this candidate represents one occurrence rather than a standalone task.
     */
    public boolean recurringOccurrence() {
        return occurrenceScheduledAt != null;
    }
}
