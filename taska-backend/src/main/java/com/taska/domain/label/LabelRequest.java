package com.taska.domain.label;

import jakarta.validation.constraints.NotBlank;

public record LabelRequest(
        @NotBlank String name,
        String color,
        Integer order,
        Boolean isFavorite
) {}
