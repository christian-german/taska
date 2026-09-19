package com.taska.domain.task.occurrence;

/** Lifecycle status stored in a persisted {@link TaskOccurrenceState}. */
public enum TaskOccurrenceStatus {

  /** The occurrence has been completed by the user. */
  DONE,

  /** The occurrence has been deleted/skipped; it is excluded from occurrence expansion. */
  SKIPPED,

  /**
   * The occurrence has been edited via a {@code THIS_ONLY} update. Its persisted state holds
   * override values for {@code title}, {@code priority}, and/or {@code scheduledAt}.
   */
  MODIFIED
}
