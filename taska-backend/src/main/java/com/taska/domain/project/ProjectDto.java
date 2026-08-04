package com.taska.domain.project;

import java.time.Instant;
import java.util.UUID;

/**
 * Transfer object representing a project returned by the API.
 *
 * @param id             unique identifier of the project
 * @param name           display name of the project
 * @param color          hex color string used for UI display (e.g. {@code "#ff5733"})
 * @param parentId       UUID of the parent project for nested/sub-projects; {@code null} for root projects
 * @param order          display position among sibling projects (maps to the entity's {@code position})
 * @param isFavorite     whether the project is starred/favourited by the user
 * @param viewStyle      preferred rendering mode: {@code LIST}, {@code BOARD}, or {@code CALENDAR}
 * @param isInboxProject {@code true} for the special default inbox project that cannot be deleted
 * @param createdAt      timestamp when the project was first created
 * @param updatedAt      timestamp of the last update to the project
 */
public record ProjectDto(
        UUID id,
        String name,
        String color,
        UUID parentId,
        Integer order,
        Boolean isFavorite,
        ViewStyle viewStyle,
        Boolean isInboxProject,
        UUID planningCalendarId,
        Instant createdAt,
        Instant updatedAt
) {}
