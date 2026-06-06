package com.taska.domain.timeentry;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a time-tracking entry in the {@code time_entries} table.
 * <p>
 * Each entry records a closed time interval ({@link #startAt} to {@link #endAt}) for a given
 * project. An optional free-text {@link #description} (max 500 chars) labels the work done,
 * while {@link #notes} holds longer supplementary text and may be cleared by passing
 * {@code null} on update.
 */
@Entity
@Table(name = "time_entries")
@Getter
@Setter
public class TimeEntry {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Start of the tracked interval; required. */
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    /** End of the tracked interval; required. Must be after {@link #startAt}. */
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    /** UUID of the project this time entry is billed/attributed to; required. */
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    /** Short label describing the work done; max 500 characters. Defaults to empty string. */
    @Column(length = 500, nullable = false)
    private String description = "";

    /**
     * Optional extended notes for the entry; stored as {@code TEXT}.
     * {@code null} means no notes; setting to {@code null} on update intentionally clears
     * any previously saved notes.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the entry was first persisted; set by {@link #onCreate()} and never updated. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Timestamp of the last modification; updated automatically by {@link #onUpdate()}. */
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
