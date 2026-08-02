package com.taska.domain.task;

import java.time.Instant;

/**
 * Request payload for deleting a task, with optional recurrence scope for recurring tasks.
 * When both fields are {@code null}, the task entity is permanently deleted regardless of
 * whether it is recurring.
 *
 * @param scope       how to apply the deletion to a recurring series:
 *                    {@code THIS_ONLY} marks the specified occurrence as SKIPPED (leaving the
 *                    rest of the series intact); {@code FROM_THIS} truncates the series so that
 *                    no occurrences are generated from {@code occurrenceScheduledAt} onwards
 * @param occurrenceScheduledAt the UTC instant of the recurring occurrence to act on; required when
 *                    {@code scope} is set
 */
public record TaskDeleteRequest(RecurrenceScope scope, Instant occurrenceScheduledAt) {}
