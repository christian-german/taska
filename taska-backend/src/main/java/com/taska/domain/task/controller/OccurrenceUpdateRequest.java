package com.taska.domain.task.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

/** New occurrence schedule; null restores the original occurrence date. */
public record OccurrenceUpdateRequest(@JsonProperty(required = true) Instant scheduledAt) {}
