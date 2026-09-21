package com.taska.notification.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Technical marker recording that one recurring occurrence was claimed for notification.
 *
 * <p>
 * This entity deliberately lives outside {@code TaskOccurrenceState}: sending a notification must not materialize a virtual occurrence or alter its
 * business state.
 */
@Entity
@Table(name = "task_occurrence_notifications")
public class TaskOccurrenceNotification {

    /** Technical identifier generated when the occurrence is claimed. */
    @Id
    private UUID id;

    /** Identifier of the recurring series that owns the occurrence. */
    @Column(name = "series_id", nullable = false)
    private UUID seriesId;

    /**
     * Stable RRULE-generated instant identifying the occurrence within its series.
     */
    @Column(name = "occurrence_scheduled_at", nullable = false)
    private Instant occurrenceScheduledAt;

    /** Instant at which the scheduler successfully claimed the occurrence. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public UUID getSeriesId() {
        return seriesId;
    }

    public Instant getOccurrenceScheduledAt() {
        return occurrenceScheduledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSeriesId(UUID seriesId) {
        this.seriesId = seriesId;
    }

    public void setOccurrenceScheduledAt(Instant occurrenceScheduledAt) {
        this.occurrenceScheduledAt = occurrenceScheduledAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
