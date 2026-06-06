package com.taska.domain.comment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a comment in the {@code comments} table.
 * <p>
 * A comment is scoped to either a task or a project — exactly one of {@link #taskId} or
 * {@link #projectId} should be set. Task-level comments are retrieved via
 * {@link CommentRepository#findByTaskIdOrderByCreatedAtAsc}; project-level comments via
 * {@link CommentRepository#findByProjectIdOrderByCreatedAtAsc}.
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * UUID of the task this comment belongs to; {@code null} for project-level comments.
     * Mutually exclusive with {@link #projectId}.
     */
    @Column(name = "task_id")
    private UUID taskId;

    /**
     * UUID of the project this comment belongs to; {@code null} for task-level comments.
     * Mutually exclusive with {@link #taskId}.
     */
    @Column(name = "project_id")
    private UUID projectId;

    /** Body text of the comment; stored as {@code TEXT} to allow arbitrarily long content. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Timestamp when the comment was first persisted; set by {@link #onCreate()} and never updated. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Initialises {@link #createdAt} on first persist. */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
