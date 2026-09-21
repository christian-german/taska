package com.taska.task.application.definition;

import com.taska.task.model.Task;
import java.time.Instant;
import java.util.Objects;

/**
 * Invariants a stored task definition must satisfy, whichever service writes it.
 *
 * <p>
 * These rules depend on nothing but the values being written, so they live here rather than on one service that the others would have to call in
 * order to validate their own concept.
 */
public final class TaskDefinitionRules {

    private TaskDefinitionRules() {
    }

    /**
     * Existing series keep their generator even before the first occurrence is acted on.
     */
    public static void assertGeneratorUnchanged(Task task, boolean recurring, Instant scheduledAt, String recurrenceRule, boolean allDay) {
        if (Boolean.TRUE.equals(task.getIsRecurring()) && (!recurring || !Objects.equals(task.getScheduledAt(), scheduledAt)
                || !Objects.equals(normalizeRecurrenceRule(task.getRecurrenceRule()), normalizeRecurrenceRule(recurrenceRule))
                || task.isAllDay() != allDay)) {
            throw new IllegalArgumentException("An existing series schedule and recurrence cannot be changed; stop the series instead");
        }
    }

    /**
     * Rejects the invalid combination of a recurring-series definition and an absolute deadline. A series describes when occurrences start; a single
     * absolute instant cannot be a deadline for all of them. Occurrence deadlines are carried by occurrence state instead.
     *
     * @param recurring whether the resulting task definition is recurring
     * @param dueAt proposed absolute deadline
     * @throws IllegalArgumentException when a recurring series is assigned a deadline
     */
    public static void assertDueAtAllowed(boolean recurring, Instant dueAt) {
        if (recurring && dueAt != null) {
            throw new IllegalArgumentException("Recurring series cannot have a dueAt deadline");
        }
    }

    /**
     * Rejects a recurring definition that has no schedule from which recurrence expansion can start.
     *
     * @param recurring whether the resulting task definition is recurring
     * @param scheduledAt proposed recurrence anchor
     * @throws IllegalArgumentException when a recurring definition has no schedule
     */
    public static void assertScheduledAtRequired(boolean recurring, Instant scheduledAt) {
        if (recurring && scheduledAt == null) {
            throw new IllegalArgumentException("Recurring series requires a scheduledAt value");
        }
    }

    /**
     * Rejects a recurring definition that carries no recurrence rule. Such a row claims to generate occurrences while holding nothing to generate
     * them from: it is excluded from calendar expansion and makes the recurrence expander throw wherever it is reached.
     *
     * @param recurring whether the resulting task definition is recurring
     * @param recurrenceRule proposed recurrence rule, already normalised
     * @throws IllegalArgumentException when a recurring definition has no rule
     */
    public static void assertRecurrenceRuleRequired(boolean recurring, String recurrenceRule) {
        if (recurring && (recurrenceRule == null || recurrenceRule.isBlank())) {
            throw new IllegalArgumentException("Recurring series requires a recurrence rule");
        }
    }

    /**
     * Converts supported recurrence aliases to the canonical RRULE form used by stored definitions. Unknown values are retained so the recurrence
     * parser remains the authority on RRULE syntax.
     *
     * @param recurrenceRule recurrence rule or supported alias
     * @return normalized recurrence rule
     */
    public static String normalizeRecurrenceRule(String recurrenceRule) {
        if (recurrenceRule == null || recurrenceRule.isBlank() || recurrenceRule.toUpperCase().startsWith("FREQ=")) {
            return recurrenceRule;
        }
        return switch (recurrenceRule.toLowerCase()) {
            case "daily" -> "FREQ=DAILY";
            case "weekly" -> "FREQ=WEEKLY";
            case "monthly" -> "FREQ=MONTHLY";
            case "yearly" -> "FREQ=YEARLY";
            default -> recurrenceRule;
        };
    }
}
