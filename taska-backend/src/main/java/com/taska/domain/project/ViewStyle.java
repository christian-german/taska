package com.taska.domain.project;

/**
 * Preferred task rendering mode for a {@link Project}.
 */
public enum ViewStyle {
    /** Tasks displayed as a flat ordered list (default). */
    LIST,
    /** Tasks displayed as cards on a Kanban-style board, grouped by section. */
    BOARD,
    /** Tasks displayed on a calendar view, grouped by due date. */
    CALENDAR
}
