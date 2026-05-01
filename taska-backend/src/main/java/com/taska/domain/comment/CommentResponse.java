package com.taska.domain.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID taskId,
        UUID projectId,
        String content,
        Instant createdAt
) {}
