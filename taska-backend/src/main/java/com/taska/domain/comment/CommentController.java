package com.taska.domain.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    /**
     * Returns comments filtered by task or project. At most one filter should be provided.
     *
     * @param task_id    optional task UUID to scope the result
     * @param project_id optional project UUID to scope the result
     * @return list of comment DTOs ordered by creation time
     */
    @GetMapping
    public List<CommentDto> getAll(@RequestParam(required = false) UUID task_id, @RequestParam(required = false) UUID project_id) {
        return commentService.findAll(task_id, project_id).stream().map(commentMapper::toDto).toList();
    }

    /**
     * Creates a new comment on a task or project. Returns HTTP 201 with the created comment DTO.
     *
     * @param req validated comment creation payload
     * @return the created comment DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@Valid @RequestBody CommentRequest req) {
        return commentMapper.toDto(commentService.create(req));
    }

    /**
     * Updates the content of an existing comment.
     *
     * @param id  the comment UUID
     * @param req the update payload containing the new content
     * @return the updated comment DTO
     */
    @PutMapping("/{id}")
    public CommentDto update(@PathVariable UUID id, @RequestBody CommentRequest req) {
        return commentMapper.toDto(commentService.update(id, req));
    }

    /**
     * Deletes the comment with the given ID. Returns HTTP 204 on success.
     *
     * @param id the comment UUID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        commentService.delete(id);
    }
}
