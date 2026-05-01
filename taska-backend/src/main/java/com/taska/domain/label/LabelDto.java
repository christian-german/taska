package com.taska.domain.label;

import java.util.UUID;

public record LabelDto(
        UUID id,
        String name,
        String color,
        Integer order,
        Boolean isFavorite
) {}
