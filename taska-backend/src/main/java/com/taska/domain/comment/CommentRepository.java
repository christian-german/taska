package com.taska.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Repository for {@link Comment} entities. */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Returns all comments attached to the given task, ordered by creation time ascending
     * (oldest first). Used when fetching task-level comments.
     */
    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);

    /**
     * Returns all comments attached to the given project, ordered by creation time ascending
     * (oldest first). Used when fetching project-level comments.
     */
    List<Comment> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
}
