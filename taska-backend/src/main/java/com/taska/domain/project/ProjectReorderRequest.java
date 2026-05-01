package com.taska.domain.project;

import java.util.UUID;

public record ProjectReorderRequest(UUID id, int order) {}
