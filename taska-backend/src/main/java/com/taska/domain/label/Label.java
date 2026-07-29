package com.taska.domain.label;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * JPA entity representing a user-defined label in the {@code labels} table.
 * <p>
 * Labels are global tags that can be attached to tasks (stored in the {@code task_labels}
 * join table via {@link com.taska.domain.task.Task#labels}). Label names are unique
 * across the installation.
 */
@Entity
@Table(name = "labels")
@Getter
@Setter
public class Label {

    /** Auto-generated UUID primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Display name of the label; required and unique across all labels. */
    @Column(nullable = false, unique = true)
    private String name;

    /** Colour identifier used for UI rendering. Defaults to {@code "charcoal"}. */
    @Column(nullable = false)
    private String color = "charcoal";

    /** Display position in the label list. Defaults to 0. */
    @Column(name = "position", nullable = false)
    private Integer position = 0;

    /** Whether the label is starred/favourited by the user. Defaults to {@code false}. */
    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;
}
