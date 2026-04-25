package com.taska.dto;

import java.time.Instant;
import java.util.UUID;

public record SectionResponse(
        UUID id,
        String name,
        UUID projectId,
        Integer order,
        Instant createdAt
) {}
