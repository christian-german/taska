package com.taska.domain.comment;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Request payload for creating or updating a comment.
 * On create, exactly one of {@code taskId} or {@code projectId} should be provided to associate
 * the comment with the correct entity. On update, only {@code content} is applied.
 *
 * @param taskId    UUID of the task to attach this comment to; {@code null} for a project-level comment
 * @param projectId UUID of the project to attach this comment to; {@code null} for a task-level comment
 * @param content   required text content of the comment; must not be blank
 */
public record CommentRequest(
        UUID taskId,
        UUID projectId,
        @NotBlank String content
) {}
