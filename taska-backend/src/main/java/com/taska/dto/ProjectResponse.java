package com.taska.dto;

import com.taska.model.ViewStyle;
import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
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
