package com.taska.domain.label.controller;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Complete mutable representation used to replace a label.
 *
 * @param name       required unique display name; must not be blank
 * @param color      required color identifier for UI display
 * @param order      required display position among all labels
 * @param isFavorite required favourite state
 */
public record LabelUpdateRequest(
        @JsonProperty(required = true) @NotBlank String name,
        @JsonProperty(required = true) @NotNull String color,
        @JsonProperty(required = true) @NotNull Integer order,
        @JsonProperty(required = true) @NotNull Boolean isFavorite
) {

    /** Rejects properties outside the label replacement contract. */
    @JsonAnySetter
    public void rejectUnknownProperty(String propertyName, Object ignoredValue) {
        throw new IllegalArgumentException("Unknown label replacement property: " + propertyName);
    }
}
