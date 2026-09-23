package com.taska.comment.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a comment in the {@code comments} table.
 *
 * <p>
 * A comment is scoped to a task, retrieved via {@link com.taska.comment.persistence.CommentRepository#findByTaskIdOrderByCreatedAtAsc}.
 */
@Entity
@Table(name = "comments")
public class Comment {
    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * UUID of the task this comment belongs to.
     */
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    /**
     * Body text of the comment; stored as {@code TEXT} to allow arbitrarily long content.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Timestamp when the comment was first persisted; set by {@link #onCreate()} and never updated.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Initialises {@link #createdAt} on first persist. */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getTaskId() {
        return this.taskId;
    }

    public String getContent() {
        return this.content;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setTaskId(final UUID taskId) {
        this.taskId = taskId;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }
}
