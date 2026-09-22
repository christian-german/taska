package com.taska.comment.adapter.http;

import java.time.Instant;
import java.util.UUID;

/**
 * Transfer object representing a comment returned by the API.
 *
 * @param id unique identifier of the comment
 * @param taskId UUID of the task this comment belongs to
 * @param content text content of the comment
 * @param createdAt timestamp when the comment was created
 */
public record CommentDto(UUID id, UUID taskId, String content, Instant createdAt) {
}
