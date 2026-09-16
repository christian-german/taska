package com.taska.domain.task.service;

import com.taska.domain.task.repository.Task;
import com.taska.domain.task.occurrence.TaskInstance;
import java.time.Instant;

/** Application result representing either a base task or one expanded recurring occurrence. */
public record TaskResult(Task task, TaskInstance taskInstance, Instant occurrenceScheduledAt) {
  public static TaskResult base(Task task) {
    return new TaskResult(task, null, null);
  }

  public static TaskResult occurrence(
      Task task, TaskInstance taskInstance, Instant occurrenceScheduledAt) {
    return new TaskResult(task, taskInstance, occurrenceScheduledAt);
  }
}
