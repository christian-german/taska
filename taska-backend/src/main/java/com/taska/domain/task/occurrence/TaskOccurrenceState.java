package com.taska.domain.task.occurrence;

import com.taska.domain.task.repository.Task;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing sparse persisted state for a recurring {@link Task} occurrence in the
 * {@code task_occurrence_states} table.
 *
 * <p>Most occurrences of a recurring task are <em>virtual</em> — generated on the fly from the
 * task's RRULE — and have no corresponding row here. A {@code TaskOccurrenceState} is created only
 * when an occurrence is explicitly acted on: completed ({@code DONE}), skipped ({@code SKIPPED}),
 * or modified ({@code MODIFIED}). The combination of {@link #seriesId} and {@link
 * #occurrenceScheduledAt} is effectively a unique key identifying a specific occurrence.
 */
@Entity
@Table(name = "task_occurrence_states")
public class TaskOccurrenceState {
  /** Auto-generated UUID primary key. */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** UUID of the recurring {@link Task} series this state belongs to. */
  @Column(name = "series_id", nullable = false)
  private UUID seriesId;

  /**
   * The RRULE-generated instant this state corresponds to. Together with {@link #seriesId}, this
   * uniquely identifies a single occurrence.
   */
  @Column(name = "occurrence_scheduled_at", nullable = false)
  private Instant occurrenceScheduledAt;

  /**
   * Override for the occurrence's planned schedule time; non-null only when the occurrence was
   * modified via a {@code THIS_ONLY} update that changed the task's {@code scheduledAt}.
   */
  @Column(name = "scheduled_at")
  private Instant scheduledAt;

  /** Optional deadline override for this recurring occurrence. */
  @Column(name = "due_at")
  private Instant dueAt;

  /** Current state of this occurrence: {@code DONE}, {@code SKIPPED}, or {@code MODIFIED}. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TaskOccurrenceStatus status;

  /**
   * Timestamp when the occurrence was completed; non-null only when {@link #status} is {@code
   * DONE}.
   */
  @Column(name = "completed_at")
  private Instant completedAt;

  /**
   * Override for the occurrence's title (task content); non-null only when the occurrence was
   * modified via a {@code THIS_ONLY} update that changed the content.
   */
  @Column(length = 1000)
  private String title;

  /**
   * Override for the occurrence's priority; non-null only when the occurrence was modified via a
   * {@code THIS_ONLY} update that changed the priority.
   */
  private Integer priority;

  /**
   * Timestamp when this state row was first persisted; set by {@link #onCreate()} and never
   * updated.
   */
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /**
   * Timestamp of the last modification to this occurrence-state row; updated automatically by
   * {@link #onUpdate()}.
   */
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

  public UUID getId() {
    return this.id;
  }

  public UUID getSeriesId() {
    return this.seriesId;
  }

  public Instant getOccurrenceScheduledAt() {
    return this.occurrenceScheduledAt;
  }

  public Instant getScheduledAt() {
    return this.scheduledAt;
  }

  public Instant getDueAt() {
    return this.dueAt;
  }

  public TaskOccurrenceStatus getStatus() {
    return this.status;
  }

  public Instant getCompletedAt() {
    return this.completedAt;
  }

  public String getTitle() {
    return this.title;
  }

  public Integer getPriority() {
    return this.priority;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public void setSeriesId(final UUID seriesId) {
    this.seriesId = seriesId;
  }

  public void setOccurrenceScheduledAt(final Instant occurrenceScheduledAt) {
    this.occurrenceScheduledAt = occurrenceScheduledAt;
  }

  public void setScheduledAt(final Instant scheduledAt) {
    this.scheduledAt = scheduledAt;
  }

  public void setDueAt(final Instant dueAt) {
    this.dueAt = dueAt;
  }

  public void setStatus(final TaskOccurrenceStatus status) {
    this.status = status;
  }

  public void setCompletedAt(final Instant completedAt) {
    this.completedAt = completedAt;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setPriority(final Integer priority) {
    this.priority = priority;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(final Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
