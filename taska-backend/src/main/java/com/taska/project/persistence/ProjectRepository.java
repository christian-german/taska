package com.taska.project.persistence;

import com.taska.project.model.Project;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /** Returns all projects ordered by their position ascending. */
    List<Project> findAllByOrderByPositionAsc();

    /**
     * Returns the inbox project, if one exists. At most one project should be flagged as inbox.
     */
    Optional<Project> findByIsInboxProjectTrue();
}
