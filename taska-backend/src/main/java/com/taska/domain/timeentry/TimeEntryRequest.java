package com.taska.domain.timeentry;

import java.time.Instant;
import java.util.UUID;

/**
 * Request payload for creating or updating a time entry.
 * On update, non-null fields overwrite the existing values. The {@code notes} field is always
 * applied — passing {@code null} intentionally clears any existing notes.
 *
 * @param startAt     UTC timestamp when the tracked period started
 * @param endAt       UTC timestamp when the tracked period ended; {@code null} represents an in-progress timer
 * @param projectId   optional UUID of the project to associate this time entry with
 * @param description short label for the time entry; defaults to an empty string on create
 * @param notes       optional longer free-text notes; a {@code null} value on update clears existing notes
 */
public record TimeEntryRequest(
        Instant startAt,
        Instant endAt,
        UUID projectId,
        String description,
        String notes
) {}
