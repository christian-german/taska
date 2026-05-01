package com.taska.domain.section;

import java.time.Instant;
import java.util.UUID;

public record SectionDto(
        UUID id,
        String name,
        UUID projectId,
        Integer order,
        Instant createdAt
) {}
