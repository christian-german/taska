package com.taska.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /** Returns all projects ordered by their position ascending. */
    List<Project> findAllByOrderByPositionAsc();

    /** Returns the inbox project, if one exists. At most one project should be flagged as inbox. */
    Optional<Project> findByIsInboxProjectTrue();
}
