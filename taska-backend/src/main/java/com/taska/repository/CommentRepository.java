package com.taska.repository;

import com.taska.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByTaskIdOrderByCreatedAtAsc(UUID taskId);
    List<Comment> findByProjectIdOrderByCreatedAtAsc(UUID projectId);
}
