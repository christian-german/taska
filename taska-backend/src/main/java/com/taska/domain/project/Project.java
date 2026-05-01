package com.taska.domain.project;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
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
}
