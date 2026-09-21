package com.taska.project.adapter.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taska.project.model.ViewStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Complete mutable representation used to replace a project. */
public record ProjectUpdateRequest(@JsonProperty(required = true) @NotBlank String name, @JsonProperty(required = true) @NotNull String color,
        @JsonProperty(required = true) UUID parentId, @JsonProperty(required = true) @NotNull Integer order,
        @JsonProperty(required = true) @NotNull Boolean isFavorite, @JsonProperty(required = true) @NotNull ViewStyle viewStyle,
        @JsonProperty(required = true) @NotNull UUID planningCalendarId) {
}
