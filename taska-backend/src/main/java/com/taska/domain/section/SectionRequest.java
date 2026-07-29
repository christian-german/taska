package com.taska.domain.section;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload for creating or updating a section.
 * On update, only non-null fields are applied.
 *
 * @param name      required display name; must not be blank
 * @param projectId required UUID of the project this section belongs to; a section must always
 *                  be associated with a project and cannot be moved between projects on update
 * @param order     display position within the project; defaults to {@code 0} on create
 */
public record SectionRequest(
        @NotBlank String name,
        @NotNull UUID projectId,
        Integer order
) {}
