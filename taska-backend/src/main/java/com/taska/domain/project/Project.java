package com.taska.domain.project;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a project in the {@code projects} table.
 * <p>
 * Projects can be nested (via {@link #parentId}) to form a hierarchy. Exactly one project per
 * installation is flagged as the inbox ({@link #isInboxProject}); this project acts as the
 * default container for tasks created without an explicit project and cannot be deleted.
 */
@Entity
@Table(name = "projects")
public class Project {
    /**
     * Auto-generated UUID primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    /**
     * Display name of the project; required.
     */
    @Column(nullable = false)
    private String name;
    /**
     * Hex color string used for UI display. Defaults to {@code "#808080"}.
     */
    @Column(nullable = false)
    private String color = "#808080";
    /**
     * UUID of the parent project; {@code null} for root-level projects.
     * Used to build the project hierarchy in the UI.
     */
    @Column(name = "parent_id")
    private UUID parentId;
    /**
     * Display position among sibling projects. Defaults to 0.
     */
    @Column(name = "position", nullable = false)
    private Integer position = 0;
    /**
     * Whether the project is starred/favourited by the user. Defaults to {@code false}.
     */
    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;
    /**
     * Preferred task rendering mode for this project.
     * Stored as a string enum; defaults to {@link ViewStyle#LIST}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "view_style", nullable = false)
    private ViewStyle viewStyle = ViewStyle.LIST;
    /**
     * Marks the special inbox project. At most one project should have this set to {@code true}.
     * Tasks created without an explicit project are placed here.
     * The inbox project cannot be deleted.
     */
    @Column(name = "is_inbox_project", nullable = false)
    private Boolean isInboxProject = false;
    @Column(name = "planning_calendar_id", nullable = false)
    private UUID planningCalendarId;
    /**
     * Timestamp when the project was first persisted; set by {@link #onCreate()} and never updated.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    /**
     * Timestamp of the last modification; updated automatically by {@link #onUpdate()}.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Initialises {@link #createdAt} and {@link #updatedAt} on first persist.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /**
     * Refreshes {@link #updatedAt} on every subsequent persist.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getColor() {
        return this.color;
    }

    public UUID getParentId() {
        return this.parentId;
    }

    public Integer getPosition() {
        return this.position;
    }

    public Boolean getIsFavorite() {
        return this.isFavorite;
    }

    public ViewStyle getViewStyle() {
        return this.viewStyle;
    }

    public Boolean getIsInboxProject() {
        return this.isInboxProject;
    }

    public UUID getPlanningCalendarId() {
        return this.planningCalendarId;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
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

    public void setParentId(final UUID parentId) {
        this.parentId = parentId;
    }

    public void setPosition(final Integer position) {
        this.position = position;
    }

    public void setIsFavorite(final Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setViewStyle(final ViewStyle viewStyle) {
        this.viewStyle = viewStyle;
    }

    public void setIsInboxProject(final Boolean isInboxProject) {
        this.isInboxProject = isInboxProject;
    }

    public void setPlanningCalendarId(final UUID planningCalendarId) {
        this.planningCalendarId = planningCalendarId;
    }

    public void setCreatedAt(final Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
