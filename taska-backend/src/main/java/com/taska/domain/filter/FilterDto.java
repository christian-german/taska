package com.taska.domain.filter;

import java.util.UUID;

/**
 * Transfer object representing a saved filter returned by the API.
 * A filter encodes a reusable task query defined by an optional project and an optional
 * date-presence constraint.
 *
 * @param id         unique identifier of the filter
 * @param name       display name of the filter
 * @param color      color identifier used for UI display
 * @param order      display position among all filters (maps to the entity's {@code position})
 * @param isFavorite whether the filter is starred/favourited by the user
 * @param projectId  optional project constraint; when set, only tasks in this project are matched
 * @param hasDate    optional date constraint: {@code true} = tasks with a due date,
 *                   {@code false} = tasks without a due date, {@code null} = no constraint on due date
 */
public record FilterDto(
        UUID id,
        String name,
        String color,
        Integer order,
        Boolean isFavorite,
        UUID projectId,
        Boolean hasDate
) {}
