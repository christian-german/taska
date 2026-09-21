package com.taska.domain.task.occurrence.service;

import java.time.Instant;

/** Application parameters for moving an occurrence or restoring its original date. */
public record TaskOccurrenceUpdateParameters(Instant scheduledAt) {}
