package com.taska.domain.label.repository;

import com.taska.domain.label.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Repository for {@link Label} entities. */
public interface LabelRepository extends JpaRepository<Label, UUID> {

    /** Returns all labels sorted by {@code position} ascending — the natural display order. */
    List<Label> findAllByOrderByPositionAsc();

}
