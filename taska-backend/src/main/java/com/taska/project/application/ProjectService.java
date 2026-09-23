package com.taska.project.application;

import com.taska.planningcalendar.application.PlanningCalendarService;
import com.taska.platform.exception.ResourceNotFoundException;
import com.taska.project.model.Project;
import com.taska.project.model.ProjectCreateParameters;
import com.taska.project.model.ProjectPlanningCalendarChangeRequested;
import com.taska.project.model.ProjectReorderParameters;
import com.taska.project.model.ProjectUpdateParameters;
import com.taska.project.persistence.ProjectRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final PlanningCalendarService planningCalendarService;
    private final ApplicationEventPublisher events;

    /**
     * Returns all projects ordered by their position ascending.
     *
     * @return list of all project entities
     */
    public List<Project> findAll() {
        return projectRepository.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the project with the given ID, or throws {@link com.taska.platform.exception.ResourceNotFoundException}.
     *
     * @param projectId the project UUID
     * @return the matching project entity
     */
    public Project findById(UUID projectId) {
        return getOrThrow(projectId);
    }

    /**
     * Creates and persists a new project from the given request. Defaults: color "#808080", position 0, not a favourite, view style LIST.
     *
     * @param projectCreateParameters application parameters for the new project
     * @return the persisted project entity
     */
    @Transactional
    public Project create(ProjectCreateParameters projectCreateParameters) {
        Project project = new Project();
        project.setName(projectCreateParameters.name());
        project.setColor(projectCreateParameters.color());
        project.setParentId(projectCreateParameters.parentId());
        project.setPosition(projectCreateParameters.position());
        project.setIsFavorite(projectCreateParameters.favorite());
        project.setViewStyle(projectCreateParameters.viewStyle());
        UUID planningCalendarId = projectCreateParameters.planningCalendarId() != null
                ? projectCreateParameters.planningCalendarId()
                : PlanningCalendarService.DEFAULT_ID;
        if (!planningCalendarService.exists(planningCalendarId)) {
            throw new ResourceNotFoundException("Planning calendar not found: " + planningCalendarId);
        }
        project.setPlanningCalendarId(planningCalendarId);
        return projectRepository.save(project);
    }

    /**
     * Replaces all mutable fields of an existing project.
     *
     * @param projectId the project UUID to update
     * @param projectUpdateParameters application parameters for the replacement
     * @return the updated project entity
     */
    @Transactional
    public Project update(UUID projectId, ProjectUpdateParameters projectUpdateParameters) {
        Project project = getOrThrow(projectId);
        project.setName(projectUpdateParameters.name());
        project.setColor(projectUpdateParameters.color());
        project.setParentId(projectUpdateParameters.parentId());
        project.setPosition(projectUpdateParameters.position());
        project.setIsFavorite(projectUpdateParameters.favorite());
        project.setViewStyle(projectUpdateParameters.viewStyle());
        if (!projectUpdateParameters.planningCalendarId().equals(project.getPlanningCalendarId())) {
            if (!planningCalendarService.exists(projectUpdateParameters.planningCalendarId())) {
                throw new ResourceNotFoundException("Planning calendar not found: " + projectUpdateParameters.planningCalendarId());
            }
            // Whoever owns the rule that scheduled work must fit its calendar vetoes here,
            // synchronously.
            events.publishEvent(new ProjectPlanningCalendarChangeRequested(projectId, projectUpdateParameters.planningCalendarId()));
            project.setPlanningCalendarId(projectUpdateParameters.planningCalendarId());
        }
        return projectRepository.save(project);
    }

    /**
     * Bulk-updates the position of multiple projects in a single operation. Projects not found in the repository are silently skipped.
     *
     * @param reorderParameters list of id/position pairs defining the new positions
     */
    @Transactional
    public void reorder(List<ProjectReorderParameters> reorderParameters) {
        reorderParameters.forEach(reorderParameter -> projectRepository.findById(reorderParameter.projectId()).ifPresent(project -> {
            project.setPosition(reorderParameter.position());
            projectRepository.save(project);
        }));
    }

    /**
     * Deletes the project with the given ID. Throws {@link IllegalStateException} when attempting to delete the inbox project, which must always
     * exist.
     *
     * @param projectId the project UUID to delete
     */
    @Transactional
    public void delete(UUID projectId) {
        Project project = getOrThrow(projectId);
        if (project.getIsInboxProject()) {
            throw new IllegalStateException("Cannot delete inbox project");
        }
        projectRepository.delete(project);
    }

    /**
     * Asserts that a project exists, for a caller that addresses a resource scoped by project.
     *
     * @param projectId the project UUID
     * @throws ResourceNotFoundException when the project does not exist
     */
    public void requireExists(UUID projectId) {
        getOrThrow(projectId);
    }

    /**
     * Returns the identifier of the inbox project, where tasks with no explicit project are placed.
     *
     * @return the inbox project identifier
     * @throws ResourceNotFoundException when no inbox project is stored
     */
    public UUID inboxProjectId() {
        return projectRepository.findByIsInboxProjectTrue().orElseThrow(() -> new ResourceNotFoundException("Inbox project not found")).getId();
    }

    /**
     * Returns the planning calendar governing a project's schedules.
     *
     * @param projectId the project UUID
     * @return the project's planning calendar identifier
     * @throws ResourceNotFoundException when the project does not exist
     */
    public UUID planningCalendarIdOf(UUID projectId) {
        return getOrThrow(projectId).getPlanningCalendarId();
    }

    /**
     * Loads a project by ID or throws {@link com.taska.platform.exception.ResourceNotFoundException} if not found.
     *
     * @param projectId the project UUID
     * @return the project entity
     */
    private Project getOrThrow(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
