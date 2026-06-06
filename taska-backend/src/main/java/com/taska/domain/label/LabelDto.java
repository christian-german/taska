package com.taska.domain.label;

import java.util.UUID;

/**
 * Transfer object representing a label returned by the API.
 *
 * @param id         unique identifier of the label
 * @param name       unique display name of the label
 * @param color      color identifier used for UI display (e.g. {@code "charcoal"} or a hex string)
 * @param order      display position among all labels (maps to the entity's {@code position})
 * @param isFavorite whether the label is starred/favourited by the user
 */
public record LabelDto(
        UUID id,
        String name,
        String color,
        Integer order,
        Boolean isFavorite
) {}
