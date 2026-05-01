package com.taska.domain.filter;

import java.util.UUID;

public record FilterDto(
        UUID id,
        String name,
        String color,
        Integer order,
        Boolean isFavorite,
        UUID projectId,
        Boolean hasDate
) {}
