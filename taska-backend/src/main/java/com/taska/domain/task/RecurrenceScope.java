package com.taska.domain.task;

/**
 * Controls which occurrences of a recurring task are affected by an update or delete operation.
 */
public enum RecurrenceScope {

    /** Apply the operation only to the single identified occurrence, leaving all others unchanged. */
    THIS_ONLY,

    /**
     * Apply the operation from the identified occurrence onwards. The original series is truncated
     * just before that occurrence, and a new task (or no task, in the case of deletion) is created
     * starting from that point.
     */
    FROM_THIS
}
