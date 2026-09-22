package com.taska.comment.application;

import com.taska.comment.model.Comment;
import com.taska.comment.model.CommentCreateParameters;
import com.taska.comment.model.CommentUpdateParameters;
import com.taska.comment.persistence.CommentRepository;
import com.taska.platform.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    /**
     * Returns comments scoped by the provided filter. When {@code taskId} is given, only comments on that task are returned. If it is {@code null},
     * all comments are returned. Results are ordered by creation time ascending.
     *
     * @param taskId optional task UUID to filter by
     * @return list of matching comment entities
     */
    @Transactional(readOnly = true)
    public List<Comment> findAll(UUID taskId) {
        if (taskId != null) {
            return commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId);
        }
        return commentRepository.findAll();
    }

    /**
     * Creates and persists a new comment associated with a task.
     *
     * @param commentCreateParameters application parameters for the new comment
     * @return the persisted comment entity
     */
    public Comment create(CommentCreateParameters commentCreateParameters) {
        Comment comment = new Comment();
        comment.setTaskId(commentCreateParameters.taskId());
        comment.setContent(commentCreateParameters.content());
        return commentRepository.save(comment);
    }

    /**
     * Updates the content of an existing comment.
     *
     * @param commentId the comment UUID to update
     * @param commentUpdateParameters application parameters containing the replacement content
     * @return the updated comment entity
     */
    public Comment update(UUID commentId, CommentUpdateParameters commentUpdateParameters) {
        Comment comment = getOrThrow(commentId);
        comment.setContent(commentUpdateParameters.content());
        return commentRepository.save(comment);
    }

    /**
     * Deletes the comment with the given ID. Throws {@link com.taska.platform.exception.ResourceNotFoundException} if not found.
     *
     * @param commentId the comment UUID to delete
     */
    public void delete(UUID commentId) {
        commentRepository.delete(getOrThrow(commentId));
    }

    /**
     * Loads a comment by ID or throws {@link com.taska.platform.exception.ResourceNotFoundException} if not found.
     *
     * @param commentId the comment UUID
     * @return the comment entity
     */
    private Comment getOrThrow(UUID commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));
    }
}
