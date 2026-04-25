package com.taska.dto;

import jakarta.validation.constraints.NotBlank;

public record LabelRequest(
        @NotBlank String name,
        String color,
        Integer order,
        Boolean isFavorite
) {}
