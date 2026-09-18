package com.taska.domain.task.service;

import java.time.Instant;

/**
 * Invariants a stored task definition must satisfy, whichever service writes it.
 *
 * <p>These rules depend on nothing but the values being written, so they live here rather than on
 * one service that the others would have to call in order to validate their own concept.
 */
final class TaskDefinitionRules {

  private TaskDefinitionRules() {}

  /**
   * Rejects the invalid combination of a recurring-series definition and an absolute deadline. A
   * series describes when occurrences start; a single absolute instant cannot be a deadline for all
   * of them. Occurrence deadlines are carried by occurrence state instead.
   *
   * @param recurring whether the resulting task definition is recurring
   * @param dueAt proposed absolute deadline
   * @throws IllegalArgumentException when a recurring series is assigned a deadline
   */
  static void assertDueAtAllowed(boolean recurring, Instant dueAt) {
    if (recurring && dueAt != null) {
      throw new IllegalArgumentException("Recurring series cannot have a dueAt deadline");
    }
  }
}
