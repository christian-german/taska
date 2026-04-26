package com.taska.repository;

import com.taska.model.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FilterRepository extends JpaRepository<Filter, UUID> {
    List<Filter> findAllByOrderByPositionAsc();
}
