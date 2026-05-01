package com.taska.domain.project;

import java.time.Instant;
import java.util.UUID;

public record ProjectDto(
        UUID id,
        String name,
        String color,
        UUID parentId,
        Integer order,
        Boolean isFavorite,
        ViewStyle viewStyle,
        Boolean isInboxProject,
        Instant createdAt,
        Instant updatedAt
) {}
