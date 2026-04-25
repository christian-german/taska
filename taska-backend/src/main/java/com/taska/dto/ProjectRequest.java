package com.taska.dto;

import com.taska.model.ViewStyle;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ProjectRequest(
        @NotBlank String name,
        String color,
        UUID parentId,
        Boolean clearParent,
        Integer order,
        Boolean isFavorite,
        ViewStyle viewStyle
) {}
