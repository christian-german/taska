package com.taska.notification.persistence;

import com.taska.notification.model.TaskOccurrenceNotification;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for atomic recurring-occurrence notification claims. */
public interface TaskOccurrenceNotificationRepository extends JpaRepository<TaskOccurrenceNotification, UUID> {

    /**
     * Atomically claims one occurrence for notification delivery.
     *
     * <p>
     * The database uniqueness constraint on {@code (series_id, occurrence_scheduled_at)} elects a single scheduler execution when runs overlap.
     *
     * @param id technical marker identifier
     * @param seriesId recurring series identifier
     * @param occurrenceScheduledAt stable RRULE-generated occurrence identity
     * @param createdAt claim timestamp
     * @return {@code 1} when this call created the marker, or {@code 0} when it was already claimed
     */
    @Modifying
    @Query(value = """
            INSERT INTO task_occurrence_notifications
                (id, series_id, occurrence_scheduled_at, created_at)
            VALUES (:id, :seriesId, :occurrenceScheduledAt, :createdAt)
            ON CONFLICT (series_id, occurrence_scheduled_at) DO NOTHING
            """, nativeQuery = true)
    int claim(
            @Param("id") UUID id,
            @Param("seriesId") UUID seriesId,
            @Param("occurrenceScheduledAt") Instant occurrenceScheduledAt,
            @Param("createdAt") Instant createdAt);

    /**
     * Removes the delivery marker for an occurrence whose effective schedule changed.
     *
     * @param seriesId recurring series identifier
     * @param occurrenceScheduledAt stable RRULE-generated occurrence identity
     */
    void deleteBySeriesIdAndOccurrenceScheduledAt(UUID seriesId, Instant occurrenceScheduledAt);
}
