package com.taska.domain.timeentry;

import java.time.Instant;
import java.util.UUID;

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
