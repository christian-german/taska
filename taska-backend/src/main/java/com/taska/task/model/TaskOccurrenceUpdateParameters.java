package com.taska.task.model;

import java.time.Instant;

/**
 * Application parameters for moving an occurrence or restoring its original date.
 */
public record TaskOccurrenceUpdateParameters(Instant scheduledAt) {
}
