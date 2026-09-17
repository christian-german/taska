package com.taska.domain.task.service;

import com.taska.domain.task.occurrence.TaskInstance;
import com.taska.domain.task.occurrence.TaskInstanceStatus;
import com.taska.domain.task.repository.Task;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Application result for one expanded occurrence of a recurring task.
 *
 * <p>An occurrence is virtual until something is persisted for it. Once a {@link TaskInstance}
 * exists, each of its non-null override fields wins over the series definition. That resolution is
 * a domain rule, not a transport concern: every adapter reads it from the accessors below instead
 * of re-deriving it, so the rule has one implementation and one set of tests.
 *
 * @param task the recurring series definition backing this occurrence
 * @param taskInstance the persisted instance when the occurrence has been completed, skipped or
 *     modified; {@code null} while the occurrence is still virtual
 * @param occurrenceScheduledAt the RRULE instant identifying this occurrence within the series;
 *     distinct from {@link #resolvedScheduledAt()}, which is where the occurrence actually sits in
 *     time once an instance has moved it
 */
public record RecurringTaskOccurrenceResult(
    Task task, TaskInstance taskInstance, Instant occurrenceScheduledAt) implements TaskResult {

  public RecurringTaskOccurrenceResult {
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(occurrenceScheduledAt, "occurrenceScheduledAt");
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("A recurring occurrence requires a recurring task");
    }
  }

  /** {@code true} while nothing has been persisted for this occurrence. */
  public boolean virtual() {
    return taskInstance == null;
  }

  /** Identifier of the persisted instance, or {@code null} while the occurrence is virtual. */
  public UUID instanceId() {
    return taskInstance == null ? null : taskInstance.getId();
  }

  /** Title overridden on the instance, falling back to the series content. */
  public String resolvedContent() {
    return taskInstance != null && taskInstance.getTitle() != null
        ? taskInstance.getTitle()
        : task.getContent();
  }

  /** Manual priority overridden on the instance, falling back to the series priority. */
  public Integer resolvedPriority() {
    return taskInstance != null && taskInstance.getPriority() != null
        ? taskInstance.getPriority()
        : task.getPriority();
  }

  /**
   * Where the occurrence actually sits in time: the instance may have moved it, otherwise it stays
   * on the RRULE instant that identifies it.
   */
  public Instant resolvedScheduledAt() {
    return taskInstance != null && taskInstance.getScheduledAt() != null
        ? taskInstance.getScheduledAt()
        : occurrenceScheduledAt;
  }

  /** Deadline overridden on the instance, falling back to the series deadline. */
  public Instant resolvedDueAt() {
    return taskInstance != null && taskInstance.getDueAt() != null
        ? taskInstance.getDueAt()
        : task.getDueAt();
  }

  /** Only a persisted instance carries completion; the series definition never does. */
  public boolean completed() {
    return taskInstance != null && taskInstance.getStatus() == TaskInstanceStatus.DONE;
  }

  /** Completion timestamp of the instance, or {@code null} when there is none. */
  public Instant completedAt() {
    return taskInstance == null ? null : taskInstance.getCompletedAt();
  }
}
