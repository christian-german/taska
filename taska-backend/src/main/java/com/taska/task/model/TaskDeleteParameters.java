package com.taska.task.model;

import java.time.Instant;

/** Application parameters for a scoped recurring-task deletion. */
public record TaskDeleteParameters(RecurrenceScope scope, Instant occurrenceScheduledAt) {
}
