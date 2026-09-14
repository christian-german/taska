package com.taska.domain.task.service;

import java.time.Instant;

/** Application parameters identifying a recurring occurrence to close or reopen. */
public record TaskCloseReopenParameters(Instant occurrenceScheduledAt) {}
