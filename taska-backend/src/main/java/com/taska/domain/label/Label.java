package com.taska.domain.label;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * JPA entity representing a user-defined label in the {@code labels} table.
 *
 * <p>Labels are global tags that can be attached to tasks (stored in the {@code task_labels} join
 * table via {@link com.taska.domain.task.Task#labels}). Label names are unique across the
 * installation.
 */
@Entity
@Table(name = "labels")
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

  public UUID getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public String getColor() {
    return this.color;
  }

  public Integer getPosition() {
    return this.position;
  }

  public Boolean getIsFavorite() {
    return this.isFavorite;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setColor(final String color) {
    this.color = color;
  }

  public void setPosition(final Integer position) {
    this.position = position;
  }

  public void setIsFavorite(final Boolean isFavorite) {
    this.isFavorite = isFavorite;
  }
}
