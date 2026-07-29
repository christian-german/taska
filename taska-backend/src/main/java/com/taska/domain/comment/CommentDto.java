package com.taska.domain.comment;

import java.time.Instant;
import java.util.UUID;

/**
 * Transfer object representing a comment returned by the API.
 * A comment is associated with either a task or a project, but not both simultaneously.
 *
 * @param id        unique identifier of the comment
 * @param taskId    UUID of the task this comment belongs to; {@code null} for project-level comments
 * @param projectId UUID of the project this comment belongs to; {@code null} for task-level comments
 * @param content   text content of the comment
 * @param createdAt timestamp when the comment was created
 */
public record CommentDto(
        UUID id,
        UUID taskId,
        UUID projectId,
        String content,
        Instant createdAt
) {}
