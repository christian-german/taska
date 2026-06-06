package com.taska.domain.section;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {

    /** Returns all sections belonging to the given project, ordered by position ascending. */
    List<Section> findByProjectIdOrderByPositionAsc(UUID projectId);

    /** Deletes all sections belonging to the given project. */
    void deleteByProjectId(UUID projectId);
}
