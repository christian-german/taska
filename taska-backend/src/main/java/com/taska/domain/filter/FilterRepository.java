package com.taska.domain.filter;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FilterRepository extends JpaRepository<Filter, UUID> {
    List<Filter> findAllByOrderByPositionAsc();
}
