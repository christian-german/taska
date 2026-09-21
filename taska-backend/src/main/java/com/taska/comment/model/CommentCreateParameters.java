package com.taska.comment.model;

import java.util.UUID;

/** Application parameters for creating a comment. */
public record CommentCreateParameters(UUID taskId, UUID projectId, String content) {
}
