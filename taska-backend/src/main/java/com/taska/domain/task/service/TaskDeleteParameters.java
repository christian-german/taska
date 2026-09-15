package com.taska.domain.task.service;

import com.taska.domain.task.occurrence.RecurrenceScope;
import java.time.Instant;

/** Application parameters for a scoped recurring-task deletion. */
public record TaskDeleteParameters(RecurrenceScope scope, Instant occurrenceScheduledAt) {}
