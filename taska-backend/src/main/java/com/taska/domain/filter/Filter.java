package com.taska.domain.filter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "filters")
@Getter
@Setter
public class Filter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color = "charcoal";

    @Column(name = "position", nullable = false)
    private Integer position = 0;

    @Column(name = "is_favorite", nullable = false)
    private Boolean isFavorite = false;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "has_date")
    private Boolean hasDate;
}
