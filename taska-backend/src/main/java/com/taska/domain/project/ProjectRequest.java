package com.taska.domain.project;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Request payload for creating or updating a project.
 * On update, only non-null fields are applied; {@code null} values leave existing values unchanged.
 *
 * @param name        required display name; must not be blank
 * @param color       hex color string for UI display; defaults to {@code "#808080"} on create
 * @param parentId    UUID of the parent project to nest this project under; {@code null} keeps the
 *                    project at root level on create; on update, ignored when {@code clearParent} is {@code true}
 * @param clearParent when {@code true} on update, removes the parent relationship regardless of
 *                    whether {@code parentId} is also provided; has no effect on create
 * @param order       display position among sibling projects; defaults to {@code 0} on create
 * @param isFavorite  whether to star/favourite the project; defaults to {@code false} on create
 * @param viewStyle   preferred rendering mode ({@code LIST}, {@code BOARD}, or {@code CALENDAR});
 *                    defaults to {@code LIST} on create
 */
public record ProjectRequest(
        @NotBlank String name,
        String color,
        UUID parentId,
        Boolean clearParent,
        Integer order,
        Boolean isFavorite,
        ViewStyle viewStyle,
        UUID planningCalendarId
) {}
