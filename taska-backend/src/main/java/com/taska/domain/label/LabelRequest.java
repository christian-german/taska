package com.taska.domain.label;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating or updating a label.
 * On update, only non-null fields are applied.
 *
 * @param name       required unique display name; must not be blank
 * @param color      color identifier for UI display; defaults to {@code "charcoal"} on create
 * @param order      display position among all labels; defaults to {@code 0} on create
 * @param isFavorite whether to star/favourite the label; defaults to {@code false} on create
 */
public record LabelRequest(
        @NotBlank String name,
        String color,
        Integer order,
        Boolean isFavorite
) {}
