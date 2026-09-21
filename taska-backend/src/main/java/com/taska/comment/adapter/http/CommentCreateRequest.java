package com.taska.comment.adapter.http;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Request payload for creating a comment. Exactly one of {@code taskId} or {@code projectId} should be provided to associate the comment with the
 * correct entity.
 *
 * @param taskId UUID of the task to attach this comment to; {@code null} for a project-level comment
 * @param projectId UUID of the project to attach this comment to; {@code null} for a task-level comment
 * @param content required text content of the comment; must not be blank
 */
public record CommentCreateRequest(UUID taskId, UUID projectId, @NotBlank String content) {
}
