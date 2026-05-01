package com.taska.domain.filter;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record FilterRequest(
        @NotBlank String name,
        String color,
        Integer order,
        Boolean isFavorite,
        UUID projectId,
        Boolean clearProject,
        Boolean hasDate
) {}
