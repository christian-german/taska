package com.taska.domain.task.service;

import java.time.Instant;

/** Application parameters for replacing supported overrides on one recurring occurrence. */
public record TaskOccurrenceUpdateParameters(
    String title, Integer priority, Instant scheduledAt, Instant dueAt) {}
