package com.taska.domain.filter;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Repository for {@link Filter} entities. */
public interface FilterRepository extends JpaRepository<Filter, UUID> {

    /** Returns all filters sorted by {@code position} ascending — the natural display order. */
    List<Filter> findAllByOrderByPositionAsc();
}
