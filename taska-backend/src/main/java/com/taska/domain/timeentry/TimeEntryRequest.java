package com.taska.domain.timeentry;

import java.time.LocalDateTime;
import java.util.UUID;

public record TimeEntryRequest(
        LocalDateTime startAt,
        LocalDateTime endAt,
        UUID projectId,
        String description,
        String notes
) {}
