package com.taska.domain.project;

import com.taska.domain.project.repository.Project;

/** Preferred task rendering mode for a {@link Project}. */
public enum ViewStyle {
  /** Tasks displayed as a flat ordered list (default). */
  LIST,
  /** Tasks displayed as cards on a Kanban-style board. */
  BOARD,
  /** Tasks displayed on a calendar view, grouped by due date. */
  CALENDAR
}
