package com.taska.domain.task.service;

import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceState;
import java.time.Instant;
import java.util.Objects;

/** Exhaustive application result for a task resource or one expanded recurring occurrence. */
public sealed interface TaskResult
    permits NonRecurringTaskResult, RecurringTaskOccurrenceResult, RecurringTaskSeriesResult {

  Task task();

  /**
   * Creates a task result from a task. The task result can be a non-recurring task result or a
   * recurring task series result.
   *
   * @param task the task to create a result from
   * @return a task result
   */
  static TaskResult base(Task task) {
    Objects.requireNonNull(task, "task");
    return Boolean.TRUE.equals(task.getIsRecurring())
        ? new RecurringTaskSeriesResult(task)
        : new NonRecurringTaskResult(task);
  }

  /**
   * Creates a task result from a recurring series and an optional persisted occurrence state.
   *
   * @param task the task to create a result from
   * @param occurrenceState the persisted occurrence state, or {@code null} for a virtual occurrence
   * @param occurrenceScheduledAt the scheduled time of the occurrence
   * @return a task result
   */
  static TaskResult occurrence(
      Task task, TaskOccurrenceState occurrenceState, Instant occurrenceScheduledAt) {
    Objects.requireNonNull(task, "task");
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("An occurrence requires a recurring task");
    }
    return new RecurringTaskOccurrenceResult(task, occurrenceState, occurrenceScheduledAt);
  }
}
