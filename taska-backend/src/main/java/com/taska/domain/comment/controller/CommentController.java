package com.taska.domain.comment.controller;

import com.taska.domain.comment.service.CommentCreateParameters;
import com.taska.domain.comment.service.CommentService;
import com.taska.domain.comment.service.CommentUpdateParameters;
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
     * @param commentCreateRequest validated comment creation payload
     * @return the created comment DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@Valid @RequestBody CommentCreateRequest commentCreateRequest) {
        CommentCreateParameters commentCreateParameters = commentMapper.toParameters(commentCreateRequest);
        return commentMapper.toDto(commentService.create(commentCreateParameters));
    }

    /**
     * Updates the content of an existing comment.
     *
     * @param commentId the comment UUID
     * @param commentUpdateRequest the replacement payload containing the new content
     * @return the updated comment DTO
     */
    @PutMapping("/{commentId}")
    public CommentDto update(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateRequest commentUpdateRequest) {
        CommentUpdateParameters commentUpdateParameters = commentMapper.toParameters(commentUpdateRequest);
        return commentMapper.toDto(commentService.update(commentId, commentUpdateParameters));
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
