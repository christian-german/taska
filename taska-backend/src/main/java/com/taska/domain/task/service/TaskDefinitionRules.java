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

  /**
   * Rejects a recurring definition that carries no recurrence rule. Such a row claims to generate
   * occurrences while holding nothing to generate them from: it is excluded from calendar expansion
   * and makes the recurrence expander throw wherever it is reached.
   *
   * @param recurring whether the resulting task definition is recurring
   * @param recurrenceRule proposed recurrence rule, already normalised
   * @throws IllegalArgumentException when a recurring definition has no rule
   */
  static void assertRecurrenceRuleRequired(boolean recurring, String recurrenceRule) {
    if (recurring && (recurrenceRule == null || recurrenceRule.isBlank())) {
      throw new IllegalArgumentException("Recurring series requires a recurrence rule");
    }
  }
}
