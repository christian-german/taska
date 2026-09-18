package com.taska.domain.task;

import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskUpdateParameters;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Task, series and occurrence builders shared by the write-operation unit tests. */
final class TaskMutationFixtures {

  private TaskMutationFixtures() {}

  static UUID randomId() {
    return UUID.randomUUID();
  }

  static Task buildNonRecurringTask(UUID id) {
    Task task = new Task();
    task.setId(id);
    task.setContent("Non-recurring task");
    task.setIsRecurring(false);
    task.setIsCompleted(false);
    task.setLabels(List.of());
    task.setPriority(4);
    return task;
  }

  static Task buildRecurringTask(UUID id) {
    Task task = new Task();
    task.setId(id);
    task.setContent("Recurring task");
    task.setIsRecurring(true);
    task.setRecurrenceRule("FREQ=DAILY");
    task.setScheduledAt(Instant.parse("2026-05-01T10:00:00Z"));
    task.setIsCompleted(false);
    task.setLabels(List.of());
    task.setPriority(4);
    return task;
  }

  static TaskOccurrenceState buildOccurrenceState(
      UUID seriesId, Instant occurrenceScheduledAt, TaskOccurrenceStatus status) {
    TaskOccurrenceState occurrenceState = new TaskOccurrenceState();
    occurrenceState.setId(randomId());
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(status);
    return occurrenceState;
  }

  static TaskUpdateParameters replacementRequest(UUID projectId, UUID parentId) {
    return new TaskUpdateParameters(
        "Replacement",
        TaskType.APPOINTMENT,
        null,
        projectId,
        parentId,
        7,
        null,
        List.of("important"),
        null,
        null,
        true,
        false,
        null,
        null,
        null);
  }

  /** Minimal task patch with only the fields required for the test. All unused fields are null. */
  static TaskPatchParameters taskRequest(
      String content, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        content,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        scope,
        occurrenceScheduledAt,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  static TaskPatchParameters taskRequest(
      String content, Integer priority, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        content,
        null,
        null,
        null,
        null,
        priority,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        scope,
        occurrenceScheduledAt,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  static TaskPatchParameters reqWithScheduledAt(
      Instant scheduledAt, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        scheduledAt,
        null,
        null,
        null,
        null,
        null,
        null,
        scope,
        occurrenceScheduledAt,
        null);
  }

  @SuppressWarnings("SameParameterValue")
  static TaskPatchParameters reqWithRRule(
      String content, String recurrenceRule, RecurrenceScope scope, Instant occurrenceScheduledAt) {
    return new TaskPatchParameters(
        content,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        recurrenceRule,
        scope,
        occurrenceScheduledAt,
        null);
  }

  static TaskCreateParameters createParameters(String content, TaskType type) {
    return new TaskCreateParameters(
        content, null, null, null, 0, null, null, null, null, false, false, null, null, null, type);
  }

  /** Turns a task into a series. A series is nothing without a rule, so this one carries it. */
  static TaskPatchParameters recurringPatch(Instant dueAt) {
    return new TaskPatchParameters(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        dueAt,
        null,
        true,
        null,
        null,
        "FREQ=DAILY",
        null,
        null,
        null);
  }
}
