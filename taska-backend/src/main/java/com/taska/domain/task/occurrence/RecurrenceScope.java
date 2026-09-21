package com.taska.domain.task.occurrence;

/** Identifies a single-occurrence operation or the boundary for stopping a recurring series. */
public enum RecurrenceScope {

  /** Apply the operation only to the single identified occurrence, leaving all others unchanged. */
  THIS_ONLY,

  /** Stop the series before the identified occurrence. Supported only for deletion. */
  FROM_THIS
}
