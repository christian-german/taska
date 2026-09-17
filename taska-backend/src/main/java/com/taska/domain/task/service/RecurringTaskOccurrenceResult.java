package com.taska.domain.task.service;

import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.repository.Task;
import java.time.Instant;
import java.util.Objects;

/** Application result for one expanded occurrence of a recurring task. */
public record RecurringTaskOccurrenceResult(
    Task task, TaskInstance taskInstance, Instant occurrenceScheduledAt) implements TaskResult {
  public RecurringTaskOccurrenceResult {
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(occurrenceScheduledAt, "occurrenceScheduledAt");
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("A recurring occurrence requires a recurring task");
    }
  }
}
