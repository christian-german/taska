package com.taska.domain.timeentry;

import java.time.Instant;
import java.util.UUID;

/**
 * Transfer object representing a time entry returned by the API.
 *
 * @param id          unique identifier of the time entry
 * @param startAt     UTC timestamp when the tracked period started
 * @param endAt       UTC timestamp when the tracked period ended; {@code null} if the timer is still running
 * @param projectId   optional UUID of the project this time was logged against
 * @param description short label for the time entry (empty string when not provided)
 * @param notes       optional longer free-text notes; {@code null} when none have been set
 * @param createdAt   timestamp when the time entry was first created
 * @param updatedAt   timestamp of the last update to the time entry
 */
public record TimeEntryDto(
        UUID id,
        Instant startAt,
        Instant endAt,
        UUID projectId,
        String description,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {}
