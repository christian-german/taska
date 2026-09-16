package com.taska.domain.label.controller;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating a label.
 *
 * @param name required unique display name; must not be blank
 * @param color color identifier for UI display; defaults to {@code "charcoal"}
 * @param order display position among all labels; defaults to {@code 0}
 * @param isFavorite whether to star/favourite the label; defaults to {@code false}
 */
public record LabelCreateRequest(
    @NotBlank String name, String color, Integer order, Boolean isFavorite) {}
