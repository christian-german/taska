package com.taska.domain.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    /**
     * Returns comments filtered by a task or project. At most one filter should be provided.
     *
     * @param taskId    optional task UUID to scope the result
     * @param projectId optional project UUID to scope the result
     * @return list of comment DTOs ordered by creation time
     */
    @GetMapping
    public List<CommentDto> getAll(
            @RequestParam(name = "taskId", required = false) UUID taskId,
            @RequestParam(name = "projectId", required = false) UUID projectId) {
        return commentService.findAll(taskId, projectId).stream().map(commentMapper::toDto).toList();
    }

    /**
     * Creates a new comment on a task or project. Returns HTTP 201 with the created comment DTO.
     *
     * @param commentRequest validated comment creation payload
     * @return the created comment DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@Valid @RequestBody CommentRequest commentRequest) {
        return commentMapper.toDto(commentService.create(commentRequest));
    }

    /**
     * Updates the content of an existing comment.
     *
     * @param commentId the comment UUID
     * @param commentRequest the update payload containing the new content
     * @return the updated comment DTO
     */
    @PutMapping("/{commentId}")
    public CommentDto update(@PathVariable UUID commentId, @RequestBody CommentRequest commentRequest) {
        return commentMapper.toDto(commentService.update(commentId, commentRequest));
    }

    /**
     * Deletes the comment with the given ID. Returns HTTP 204 on success.
     *
     * @param commentId the comment UUID to delete
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID commentId) {
        commentService.delete(commentId);
    }
}
