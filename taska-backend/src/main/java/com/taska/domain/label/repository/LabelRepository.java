package com.taska.domain.label.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for {@link Label} entities. */
public interface LabelRepository extends JpaRepository<Label, UUID> {

  /** Returns all labels sorted by {@code position} ascending — the natural display order. */
  List<Label> findAllByOrderByPositionAsc();
}
