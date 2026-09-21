package com.taska.task.model;

import java.util.Objects;

/** Application result for one persisted non-recurring task. */
public record NonRecurringTaskResult(Task task) implements TaskResult {
    public NonRecurringTaskResult {
        Objects.requireNonNull(task, "task");
        if (Boolean.TRUE.equals(task.getIsRecurring())) {
            throw new IllegalArgumentException("A non-recurring result cannot contain a recurring task");
        }
    }
}
