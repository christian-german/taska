package com.taska.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "labels")
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String color = "charcoal";

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }
}
