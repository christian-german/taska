package com.taska.domain.task;

/**
 * Lifecycle state of a persisted {@link TaskInstance} (a recurring task occurrence).
 */
public enum TaskInstanceStatus {

    /** The occurrence has been completed by the user. */
    DONE,

    /** The occurrence has been deleted/skipped; it is excluded from occurrence expansion. */
    SKIPPED,

    /**
     * The occurrence has been edited via a {@code THIS_ONLY} update.
     * The instance holds override values for {@code title}, {@code priority}, and/or {@code scheduledAt}.
     */
    MODIFIED
}
