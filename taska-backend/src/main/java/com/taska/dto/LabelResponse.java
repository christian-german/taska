package com.taska.dto;

import java.util.UUID;

public record LabelResponse(
        UUID id,
        String name,
        String color,
        Integer order,
        Boolean isFavorite
) {}
