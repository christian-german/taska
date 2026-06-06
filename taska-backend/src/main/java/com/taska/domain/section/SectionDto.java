package com.taska.domain.section;

import java.time.Instant;
import java.util.UUID;

/**
 * Transfer object representing a section returned by the API.
 *
 * @param id        unique identifier of the section
 * @param name      display name of the section
 * @param projectId UUID of the project this section belongs to
 * @param order     display position within the project (maps to the entity's {@code position})
 * @param createdAt timestamp when the section was first created
 */
public record SectionDto(
        UUID id,
        String name,
        UUID projectId,
        Integer order,
        Instant createdAt
) {}
