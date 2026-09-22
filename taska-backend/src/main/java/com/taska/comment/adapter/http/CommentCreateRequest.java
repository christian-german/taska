package com.taska.comment.adapter.http;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request payload for creating a comment.
 *
 * @param taskId required UUID of the task to attach this comment to
 * @param content required text content of the comment; must not be blank
 */
public record CommentCreateRequest(@NotNull UUID taskId, @NotBlank String content) {
}
