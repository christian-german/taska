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

    @GetMapping
    public List<CommentDto> getAll(@RequestParam(required = false) UUID task_id, @RequestParam(required = false) UUID project_id) {
        return commentService.findAll(task_id, project_id).stream().map(commentMapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@Valid @RequestBody CommentRequest req) {
        return commentMapper.toDto(commentService.create(req));
    }

    @PutMapping("/{id}")
    public CommentDto update(@PathVariable UUID id, @RequestBody CommentRequest req) {
        return commentMapper.toDto(commentService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        commentService.delete(id);
    }
}
