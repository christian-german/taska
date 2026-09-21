package com.taska.domain.task.definition.repository;

import com.taska.domain.task.definition.TaskType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity representing a task in the {@code tasks} table.
 *
 * <p>A task can be a top-level item, a subtask (via {@code parentId}), or a recurring series. For
 * recurring tasks, sparse occurrence state is tracked separately by the occurrence subfeature.
 * Labels are stored in a join table ({@code task_labels}) and loaded eagerly.
 */
@Entity
@Table(name = "tasks")
public class Task {
  /** Auto-generated UUID primary key. */
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** Title / main text of the task; required, max 1000 characters. */
  @Column(nullable = false, length = 1000)
  private String content;

  /** Whether this item is work to complete or an appointment to attend. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private TaskType type = TaskType.TODO;

  /** Optional longer description stored as an unbounded TEXT column. */
  @Column(columnDefinition = "TEXT")
  private String description;

  /**
   * Denormalised reference to the owning project. {@code null} for subtasks that inherit their
   * project from the parent.
   */
  @Column(name = "project_id")
  private UUID projectId;

  /** Parent task UUID; non-null only for subtasks. */
  @Column(name = "parent_id")
  private UUID parentId;

  /** Display position within the task's project or parent. Defaults to 0. */
  @Column(name = "position", nullable = false)
  private Integer position = 0;

  /**
   * Optional urgency level: 1 = urgent, 2 = high, 3 = medium, 4 = normal. A {@code null} value
   * means no manual priority has been assigned.
   */
  @Column private Integer priority;

  /**
   * Label names attached to this task. Persisted in the {@code task_labels} join table and always
   * fetched eagerly to avoid lazy-loading issues during serialisation.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "task_labels", joinColumns = @JoinColumn(name = "task_id"))
  @Column(name = "label")
  private List<String> labels = new ArrayList<>();

  /** Whether the task has been completed. Defaults to {@code false}. */
  @Column(name = "is_completed", nullable = false)
  private Boolean isCompleted = false;

  /** Planned schedule time in UTC; {@code null} when the task is unscheduled. */
  @Column(name = "scheduled_at")
  private Instant scheduledAt;

  /** Deadline in UTC; {@code null} when the task has no due date. */
  @Column(name = "due_at")
  private Instant dueAt;

  /** When {@code true}, the scheduled time represents an all-day event with no specific time. */
  @Column(name = "all_day", nullable = false)
  private boolean allDay = false;

  /** Whether this task repeats according to {@link #recurrenceRule}. Defaults to {@code false}. */
  @Column(name = "is_recurring", nullable = false)
  private Boolean isRecurring = false;

  /** Optional time estimate for the task in minutes. */
  @Column(name = "estimate_minutes")
  private Integer estimateMinutes;

  /**
   * Raw context captured when the task was created via an @-mention (used by mobile clients). Max
   * 100 characters.
   */
  @Column(name = "mention_context", length = 100)
  private String mentionContext;

  /**
   * iCal4j RRULE string defining the recurrence pattern (e.g. {@code "FREQ=DAILY"}). Only
   * meaningful when {@link #isRecurring} is {@code true}. Max 100 characters.
   */
  @Column(name = "recurrence_rule", length = 100)
  private String recurrenceRule;

  /**
   * Instant at which the recurrence series is truncated. Occurrences at or after this instant are
   * not generated. Set by the {@code FROM_THIS} series-stopping deletion operation.
   */
  @Column(name = "rrule_ends_at")
  private Instant rruleEndsAt;

  /** Timestamp when the task was first persisted; set by {@link #onCreate()} and never updated. */
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  /** Timestamp of the last modification; updated automatically by {@link #onUpdate()}. */
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  /** Timestamp when the task was completed; {@code null} if still open. */
  @Column(name = "completed_at")
  private Instant completedAt;

  /**
   * Guards against duplicate push notifications. Set to {@code true} after a notification is sent;
   * reset to {@code false} whenever {@link #scheduledAt} is changed so the task can fire a new
   * notification.
   */
  @Column(name = "is_notified", nullable = false)
  private Boolean isNotified = false;

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

  public String getContent() {
    return this.content;
  }

  public TaskType getType() {
    return this.type;
  }

  public String getDescription() {
    return this.description;
  }

  public UUID getProjectId() {
    return this.projectId;
  }

  public UUID getParentId() {
    return this.parentId;
  }

  public Integer getPosition() {
    return this.position;
  }

  public Integer getPriority() {
    return this.priority;
  }

  public List<String> getLabels() {
    return this.labels;
  }

  public Boolean getIsCompleted() {
    return this.isCompleted;
  }

  public Instant getScheduledAt() {
    return this.scheduledAt;
  }

  public Instant getDueAt() {
    return this.dueAt;
  }

  public boolean isAllDay() {
    return this.allDay;
  }

  public Boolean getIsRecurring() {
    return this.isRecurring;
  }

  public Integer getEstimateMinutes() {
    return this.estimateMinutes;
  }

  public String getMentionContext() {
    return this.mentionContext;
  }

  public String getRecurrenceRule() {
    return this.recurrenceRule;
  }

  public Instant getRruleEndsAt() {
    return this.rruleEndsAt;
  }

  public Instant getCreatedAt() {
    return this.createdAt;
  }

  public Instant getUpdatedAt() {
    return this.updatedAt;
  }

  public Instant getCompletedAt() {
    return this.completedAt;
  }

  public Boolean getIsNotified() {
    return this.isNotified;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public void setContent(final String content) {
    this.content = content;
  }

  public void setType(final TaskType type) {
    this.type = type;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setProjectId(final UUID projectId) {
    this.projectId = projectId;
  }

  public void setParentId(final UUID parentId) {
    this.parentId = parentId;
  }

  public void setPosition(final Integer position) {
    this.position = position;
  }

  public void setPriority(final Integer priority) {
    this.priority = priority;
  }

  public void setLabels(final List<String> labels) {
    this.labels = labels;
  }

  public void setIsCompleted(final Boolean isCompleted) {
    this.isCompleted = isCompleted;
  }

  public void setScheduledAt(final Instant scheduledAt) {
    this.scheduledAt = scheduledAt;
  }

  public void setDueAt(final Instant dueAt) {
    this.dueAt = dueAt;
  }

  public void setAllDay(final boolean allDay) {
    this.allDay = allDay;
  }

  public void setIsRecurring(final Boolean isRecurring) {
    this.isRecurring = isRecurring;
  }

  public void setEstimateMinutes(final Integer estimateMinutes) {
    this.estimateMinutes = estimateMinutes;
  }

  public void setMentionContext(final String mentionContext) {
    this.mentionContext = mentionContext;
  }

  public void setRecurrenceRule(final String recurrenceRule) {
    this.recurrenceRule = recurrenceRule;
  }

  public void setRruleEndsAt(final Instant rruleEndsAt) {
    this.rruleEndsAt = rruleEndsAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setUpdatedAt(final Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public void setCompletedAt(final Instant completedAt) {
    this.completedAt = completedAt;
  }

  public void setIsNotified(final Boolean isNotified) {
    this.isNotified = isNotified;
  }
}
