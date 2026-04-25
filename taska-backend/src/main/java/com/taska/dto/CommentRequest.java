package com.taska.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CommentRequest(
        UUID taskId,
        UUID projectId,
        @NotBlank String content
) {}
