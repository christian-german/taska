package com.taska.domain.project.service;

import java.util.UUID;

/** Application parameters for one entry in a project reorder operation. */
public record ProjectReorderParameters(UUID projectId, int position) {}
