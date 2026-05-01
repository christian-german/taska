package com.taska.domain.comment;

import com.taska.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepo;

    public CommentService(CommentRepository commentRepo) {
        this.commentRepo = commentRepo;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findAll(UUID taskId, UUID projectId) {
        if (taskId != null) return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId).stream().map(this::toResponse).toList();
        if (projectId != null) return commentRepo.findByProjectIdOrderByCreatedAtAsc(projectId).stream().map(this::toResponse).toList();
        return commentRepo.findAll().stream().map(this::toResponse).toList();
    }

    public CommentResponse create(CommentRequest req) {
        Comment c = new Comment();
        c.setTaskId(req.taskId());
        c.setProjectId(req.projectId());
        c.setContent(req.content());
        return toResponse(commentRepo.save(c));
    }

    public CommentResponse update(UUID id, CommentRequest req) {
        Comment c = getOrThrow(id);
        c.setContent(req.content());
        return toResponse(commentRepo.save(c));
    }

    public void delete(UUID id) {
        commentRepo.delete(getOrThrow(id));
    }

    private Comment getOrThrow(UUID id) {
        return commentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));
    }

    public CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getTaskId(), c.getProjectId(), c.getContent(), c.getCreatedAt());
    }
}
