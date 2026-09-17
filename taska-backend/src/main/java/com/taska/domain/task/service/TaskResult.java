package com.taska.domain.task.service;

import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.repository.Task;
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
   * Creates a task result from a task and a task instance.
   *
   * @param task the task to create a result from
   * @param taskInstance the task instance to create a result from
   * @param occurrenceScheduledAt the scheduled time of the occurrence
   * @return a task result
   */
  static TaskResult occurrence(
      Task task, TaskInstance taskInstance, Instant occurrenceScheduledAt) {
    Objects.requireNonNull(task, "task");
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("An occurrence requires a recurring task");
    }
    return new RecurringTaskOccurrenceResult(task, taskInstance, occurrenceScheduledAt);
  }
}
