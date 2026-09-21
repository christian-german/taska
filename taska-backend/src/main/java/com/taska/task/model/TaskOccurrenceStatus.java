package com.taska.task.model;

/** Lifecycle status stored in a persisted {@link TaskOccurrenceState}. */
public enum TaskOccurrenceStatus {

    /** The occurrence has been completed by the user. */
    DONE,

    /**
     * The occurrence has been deleted/skipped; it is excluded from occurrence expansion.
     */
    SKIPPED,

    /**
     * Open occurrence with a schedule override, historical customizations, or detached state.
     */
    MODIFIED
}
