package com.taska.task.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Signals that one recurring occurrence now fires at a different instant.
 *
 * <p>
 * A one-time delivery already claimed for the previous schedule no longer applies: the occurrence must become eligible again at its replacement
 * schedule.
 *
 * @param seriesId recurring-series identifier
 * @param occurrenceScheduledAt stable schedule identity of the rescheduled occurrence
 */
public record TaskOccurrenceRescheduledEvent(UUID seriesId, Instant occurrenceScheduledAt) {
}
