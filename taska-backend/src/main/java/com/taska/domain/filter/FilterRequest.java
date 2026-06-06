package com.taska.domain.filter;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Request payload for creating or updating a filter.
 * On update, only non-null fields are applied.
 *
 * @param name         required display name; must not be blank
 * @param color        color identifier for UI display; defaults to {@code "charcoal"} on create
 * @param order        display position among all filters; defaults to {@code 0} on create
 * @param isFavorite   whether to star/favourite the filter; defaults to {@code false} on create
 * @param projectId    optional project to scope the filter to; when provided, only tasks in this
 *                     project are returned by {@code GET /filters/{id}/tasks};
 *                     on update, ignored when {@code clearProject} is {@code true}
 * @param clearProject when {@code true} on update, removes the project constraint so the filter
 *                     applies across all projects; has no effect on create
 * @param hasDate      optional due-date presence constraint: {@code true} = match tasks that have a
 *                     due date, {@code false} = match tasks without a due date,
 *                     {@code null} = no constraint on due date
 */
public record FilterRequest(
        @NotBlank String name,
        String color,
        Integer order,
        Boolean isFavorite,
        UUID projectId,
        Boolean clearProject,
        Boolean hasDate
) {}
