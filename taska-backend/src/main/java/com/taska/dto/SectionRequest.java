package com.taska.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SectionRequest(
        @NotBlank String name,
        @NotNull UUID projectId,
        Integer order
) {}
