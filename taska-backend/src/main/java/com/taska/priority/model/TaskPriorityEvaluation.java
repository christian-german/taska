package com.taska.priority.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "task_priority_evaluation")
public class TaskPriorityEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "task_id", nullable = false, unique = true)
    private UUID taskId;

    @Column(nullable = false)
    private int score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "components_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode components;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt;

    public UUID getId() {
        return this.id;
    }

    public UUID getTaskId() {
        return this.taskId;
    }

    public int getScore() {
        return this.score;
    }

    public JsonNode getComponents() {
        return this.components;
    }

    public Instant getComputedAt() {
        return this.computedAt;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public void setTaskId(final UUID taskId) {
        this.taskId = taskId;
    }

    public void setScore(final int score) {
        this.score = score;
    }

    public void setComponents(final JsonNode components) {
        this.components = components;
    }

    public void setComputedAt(final Instant computedAt) {
        this.computedAt = computedAt;
    }
}
