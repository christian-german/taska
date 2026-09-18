package com.taska.domain.task.service;

import com.taska.domain.task.occurrence.TaskOccurrenceState;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.repository.Task;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Application result for one expanded occurrence of a recurring task.
 *
 * <p>An occurrence is virtual until something is persisted for it. Once a {@link
 * TaskOccurrenceState} exists, each of its non-null override fields wins over the series
 * definition. That resolution is a domain rule, not a transport concern: every adapter reads it
 * from the accessors below instead of re-deriving it, so the rule has one implementation and one
 * set of tests.
 *
 * @param task the recurring series definition backing this occurrence
 * @param occurrenceState persisted state when the occurrence has been completed, skipped or
 *     modified; {@code null} while the occurrence is still virtual
 * @param occurrenceScheduledAt the RRULE instant identifying this occurrence within the series;
 *     distinct from {@link #resolvedScheduledAt()}, which is where the occurrence actually sits in
 *     time once persisted state has moved it
 */
public record RecurringTaskOccurrenceResult(
    Task task, TaskOccurrenceState occurrenceState, Instant occurrenceScheduledAt)
    implements TaskResult {

  public RecurringTaskOccurrenceResult {
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(occurrenceScheduledAt, "occurrenceScheduledAt");
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("A recurring occurrence requires a recurring task");
    }
  }

  /** {@code true} while nothing has been persisted for this occurrence. */
  public boolean virtual() {
    return occurrenceState == null;
  }

  /**
   * {@code true} when the series no longer generates this occurrence. The state then stands on its
   * own: it keeps its series and its date, but overlays nothing.
   */
  public boolean detached() {
    return occurrenceState != null && occurrenceState.isDetached();
  }

  /** Identifier of the persisted state, or {@code null} while the occurrence is virtual. */
  public UUID occurrenceStateId() {
    return occurrenceState == null ? null : occurrenceState.getId();
  }

  /** Title overridden in occurrence state, falling back to the series content. */
  public String resolvedContent() {
    return occurrenceState != null && occurrenceState.getTitle() != null
        ? occurrenceState.getTitle()
        : task.getContent();
  }

  /** Manual priority overridden in occurrence state, falling back to the series priority. */
  public Integer resolvedPriority() {
    return occurrenceState != null && occurrenceState.getPriority() != null
        ? occurrenceState.getPriority()
        : task.getPriority();
  }

  /**
   * Where the occurrence actually sits in time: persisted state may have moved it, otherwise it
   * stays on the RRULE instant that identifies it.
   */
  public Instant resolvedScheduledAt() {
    return occurrenceState != null && occurrenceState.getScheduledAt() != null
        ? occurrenceState.getScheduledAt()
        : occurrenceScheduledAt;
  }

  /** Deadline explicitly persisted for this occurrence, or null when it has no override. */
  public Instant resolvedDueAt() {
    return occurrenceState != null ? occurrenceState.getDueAt() : null;
  }

  /** Only persisted occurrence state carries completion; the series definition never does. */
  public boolean completed() {
    return occurrenceState != null && occurrenceState.getStatus() == TaskOccurrenceStatus.DONE;
  }

  /** Completion timestamp in persisted state, or {@code null} when there is none. */
  public Instant completedAt() {
    return occurrenceState == null ? null : occurrenceState.getCompletedAt();
  }
}
