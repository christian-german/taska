package com.taska.domain.task.service;

import com.taska.domain.task.repository.Task;
import java.util.Objects;

/** Application result for one persisted recurring-series definition. */
public record RecurringTaskSeriesResult(Task task) implements TaskResult {
  public RecurringTaskSeriesResult {
    Objects.requireNonNull(task, "task");
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("A recurring-series result requires a recurring task");
    }
  }
}
