package com.taska.comment.adapter.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/** Complete mutable representation used to replace a comment. */
public record CommentUpdateRequest(@JsonProperty(required = true) @NotBlank String content) {
}
