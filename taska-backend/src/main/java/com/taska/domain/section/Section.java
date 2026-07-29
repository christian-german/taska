package com.taska.domain.section;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a section within a project in the {@code sections} table.
 * Sections group tasks inside a project and define columns on a board view.
 * A section always belongs to exactly one project and cannot be moved between projects.
 */
@Entity
@Table(name = "sections")
@Getter
@Setter
public class Section {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Display name of the section; required. */
    @Column(nullable = false)
    private String name;

    /** UUID of the project this section belongs to; required (a section cannot be project-less). */
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    /** Display position within the project's section list. Defaults to 0. */
    @Column(name = "position", nullable = false)
    private Integer position = 0;

    /** Timestamp when the section was first persisted; set by {@link #onCreate()} and never updated. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Initialises {@link #createdAt} on first persist. */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
