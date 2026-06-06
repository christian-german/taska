package com.taska.domain.label;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Repository for {@link Label} entities. */
public interface LabelRepository extends JpaRepository<Label, UUID> {

    /** Returns all labels sorted by {@code position} ascending — the natural display order. */
    List<Label> findAllByOrderByPositionAsc();

    /**
     * Returns {@code true} if a label with the given name already exists.
     * Used to enforce the unique-name constraint before persisting a new label.
     */
    boolean existsByName(String name);
}
