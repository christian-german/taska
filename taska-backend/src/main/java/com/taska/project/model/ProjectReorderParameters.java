package com.taska.project.model;

import java.util.UUID;

/** Application parameters for one entry in a project reorder operation. */
public record ProjectReorderParameters(UUID projectId, int position) {
}
