package com.taska.domain.label;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "labels")
@Getter
@Setter
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
}
