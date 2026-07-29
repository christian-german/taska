package com.taska.domain.filter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA entity representing a saved task filter in the {@code filters} table.
 * <p>
 * A filter is a named, reusable query that scopes the task list by project and/or date
 * presence. All filter criteria are optional; a filter with no criteria matches all tasks.
 */
@Entity
@Table(name = "filters")
@Getter
@Setter
public class Filter {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Display name of the filter; required. */
    @Column(nullable = false)
    private String name;

    /** Colour identifier used for UI rendering. Defaults to {@code "charcoal"}. */
    @Column(nullable = false)
    private String color = "charcoal";

    /** Display position in the filter list. Defaults to 0. */
    @Column(name = "position", nullable = false)
    private Integer position = 0;

    /** Whether the filter is starred/favourited by the user. Defaults to {@code false}. */
    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    /**
     * Constrains the filter to tasks belonging to this project; {@code null} means no project
     * constraint (all projects are included).
     */
    @Column(name = "project_id")
    private UUID projectId;

    /**
     * Tri-state date filter: {@code true} — only tasks with a due date; {@code false} — only
     * tasks without a due date; {@code null} — no date constraint.
     */
    @Column(name = "has_date")
    private Boolean hasDate;
}
