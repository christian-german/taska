package com.taska.domain.task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a persisted occurrence of a recurring {@link Task} in the
 * {@code task_instances} table.
 * <p>
 * Most occurrences of a recurring task are <em>virtual</em> — generated on the fly from the
 * task's RRULE — and have no corresponding row here. A {@code TaskInstance} is created only when
 * an occurrence is explicitly acted on: completed ({@code DONE}), skipped ({@code SKIPPED}), or
 * modified ({@code MODIFIED}). The combination of {@link #taskId} and {@link #occurrenceScheduledAt} is
 * effectively a unique key identifying a specific occurrence.
 */
@Entity
@Table(name = "task_instances")
@Getter
@Setter
public class TaskInstance {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** UUID of the parent recurring {@link Task} this instance belongs to. */
    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    /**
     * The RRULE-generated instant this instance corresponds to.
     * Together with {@link #taskId}, this uniquely identifies a single occurrence.
     */
    @Column(name = "occurrence_scheduled_at", nullable = false)
    private Instant occurrenceScheduledAt;

    /**
     * Override for the occurrence's planned schedule time; non-null only when the occurrence was modified
     * via a {@code THIS_ONLY} update that changed the task's {@code scheduledAt}.
     */
    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    /** Optional deadline override for this recurring occurrence. */
    @Column(name = "due_at")
    private Instant dueAt;

    /** Current state of this occurrence: {@code DONE}, {@code SKIPPED}, or {@code MODIFIED}. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskInstanceStatus status;

    /** Timestamp when the occurrence was completed; non-null only when {@link #status} is {@code DONE}. */
    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Override for the occurrence's title (task content); non-null only when the occurrence was
     * modified via a {@code THIS_ONLY} update that changed the content.
     */
    @Column(length = 1000)
    private String title;

    /**
     * Override for the occurrence's priority; non-null only when the occurrence was modified
     * via a {@code THIS_ONLY} update that changed the priority.
     */
    private Integer priority;

    /** Timestamp when this instance row was first persisted; set by {@link #onCreate()} and never updated. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the last modification to this instance row; updated automatically by {@link #onUpdate()}. */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Initialises {@link #createdAt} and {@link #updatedAt} on first persist. */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /** Refreshes {@link #updatedAt} on every subsequent persist. */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
