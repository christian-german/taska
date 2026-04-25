package com.taska.dto;

import java.util.UUID;

public record ProjectReorderRequest(UUID id, int order) {}
