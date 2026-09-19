package com.taska.domain.project.controller;

import com.taska.domain.project.ViewStyle;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Request payload for creating a project.
 *
 * @param name required display name; must not be blank
 * @param color hex color string for UI display; defaults to {@code "#808080"} on create
 * @param parentId UUID of the parent project to nest this project under; {@code null} keeps the
 *     project at root level on creation
 * @param order display position among sibling projects; defaults to {@code 0} on create
 * @param isFavorite whether to star/favourite the project; defaults to {@code false} on create
 * @param viewStyle preferred rendering mode ({@code LIST}, {@code BOARD}, or {@code CALENDAR});
 *     defaults to {@code LIST} on create
 */
public record ProjectCreateRequest(
    @NotBlank String name,
    String color,
    UUID parentId,
    Integer order,
    Boolean isFavorite,
    ViewStyle viewStyle,
    UUID planningCalendarId) {}
