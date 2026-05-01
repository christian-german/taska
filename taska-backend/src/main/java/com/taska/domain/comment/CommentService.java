package com.taska.domain.comment;

import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;

    @Transactional(readOnly = true)
    public List<Comment> findAll(UUID taskId, UUID projectId) {
        if (taskId != null) return commentRepo.findByTaskIdOrderByCreatedAtAsc(taskId);
        if (projectId != null) return commentRepo.findByProjectIdOrderByCreatedAtAsc(projectId);
        return commentRepo.findAll();
    }

    public Comment create(CommentRequest req) {
        Comment comment = new Comment();
        comment.setTaskId(req.taskId());
        comment.setProjectId(req.projectId());
        comment.setContent(req.content());
        return commentRepo.save(comment);
    }

    public Comment update(UUID id, CommentRequest req) {
        Comment c = getOrThrow(id);
        c.setContent(req.content());
        return commentRepo.save(c);
    }

    public void delete(UUID id) {
        commentRepo.delete(getOrThrow(id));
    }

    private Comment getOrThrow(UUID id) {
        return commentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));
    }
}
