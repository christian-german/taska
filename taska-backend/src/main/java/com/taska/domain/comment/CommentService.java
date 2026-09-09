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

    private final CommentRepository commentRepository;

    /**
     * Returns comments scoped by the provided filter. When {@code taskId} is given, only comments
     * on that task are returned. When {@code projectId} is given, only project-level comments are
     * returned. If neither is provided, all comments are returned.
     * Results are ordered by creation time ascending.
     *
     * @param taskId    optional task UUID to filter by
     * @param projectId optional project UUID to filter by
     * @return list of matching comment entities
     */
    @Transactional(readOnly = true)
    public List<Comment> findAll(UUID taskId, UUID projectId) {
        if (taskId != null) {
            return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        }
        if (projectId != null) {
            return commentRepository.findByProjectIdOrderByCreatedAtAsc(projectId);
        }
        return commentRepository.findAll();
    }

    /**
     * Creates and persists a new comment associated with a task or a project.
     *
     * @param commentRequest the comment creation payload
     * @return the persisted comment entity
     */
    public Comment create(CommentRequest commentRequest) {
        Comment comment = new Comment();
        comment.setTaskId(commentRequest.taskId());
        comment.setProjectId(commentRequest.projectId());
        comment.setContent(commentRequest.content());
        return commentRepository.save(comment);
    }

    /**
     * Updates the content of an existing comment.
     *
     * @param id  the comment UUID to update
     * @param commentRequest the update payload containing the new content
     * @return the updated comment entity
     */
    public Comment update(UUID id, CommentRequest commentRequest) {
        Comment comment = getOrThrow(id);
        comment.setContent(commentRequest.content());
        return commentRepository.save(comment);
    }

    /**
     * Deletes the comment with the given ID.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the comment UUID to delete
     */
    public void delete(UUID id) {
        commentRepository.delete(getOrThrow(id));
    }

    /**
     * Loads a comment by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the comment UUID
     * @return the comment entity
     */
    private Comment getOrThrow(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + id));
    }
}
