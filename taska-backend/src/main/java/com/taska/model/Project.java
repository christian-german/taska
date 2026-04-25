package com.taska.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color = "charcoal";

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "view_style", nullable = false)
    private ViewStyle viewStyle = ViewStyle.LIST;

    @Column(name = "is_inbox_project", nullable = false)
    private Boolean isInboxProject = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }
    public ViewStyle getViewStyle() { return viewStyle; }
    public void setViewStyle(ViewStyle viewStyle) { this.viewStyle = viewStyle; }
    public Boolean getIsInboxProject() { return isInboxProject; }
    public void setIsInboxProject(Boolean isInboxProject) { this.isInboxProject = isInboxProject; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
