package com.taska.domain.project;

import java.util.UUID;

/**
 * A single entry in a bulk project reorder operation.
 *
 * @param id    UUID of the project whose position should be updated
 * @param order new display position to assign to the project
 */
public record ProjectReorderRequest(UUID id, int order) {}
