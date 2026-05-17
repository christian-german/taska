package com.taska.domain.task;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(nullable = false)
    private Integer priority = 1;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_labels", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "label")
    private List<String> labels = new ArrayList<>();

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "due_at")
    private Instant dueAt;

    @Column(name = "all_day", nullable = false)
    private boolean allDay = false;

    @Column(name = "is_recurring", nullable = false)
    private Boolean isRecurring = false;

    @Column(name = "estimate_minutes")
    private Integer estimateMinutes;

    @Column(name = "mention_context", length = 100)
    private String mentionContext;

    @Column(name = "recurrence_rule", length = 100)
    private String recurrenceRule;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "is_notified", nullable = false)
    private Boolean isNotified = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
