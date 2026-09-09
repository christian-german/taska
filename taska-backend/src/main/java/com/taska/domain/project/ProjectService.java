package com.taska.domain.project;

import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import com.taska.exception.ResourceNotFoundException;
import com.taska.domain.planningcalendar.PlanningCalendarRepository;
import com.taska.domain.planningcalendar.PlanningCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final PlanningCalendarRepository planningCalendarRepository;
    private final PlanningCalendarService planningCalendarService;

    /**
     * Returns all projects ordered by their position ascending.
     *
     * @return list of all project entities
     */
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the project with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param id the project UUID
     * @return the matching project entity
     */
    @Transactional(readOnly = true)
    public Project findById(UUID projectId) {
        return getOrThrow(projectId);
    }

    /**
     * Creates and persists a new project from the given request.
     * Defaults: color "#808080", position 0, not a favourite, view style LIST.
     *
     * @param projectRequest the project creation payload
     * @return the persisted project entity
     */
    public Project create(ProjectRequest projectRequest) {
        Project project = new Project();
        project.setName(projectRequest.name());
        project.setColor(projectRequest.color() != null ? projectRequest.color() : "#808080");
        project.setParentId(projectRequest.parentId());
        project.setPosition(projectRequest.order() != null ? projectRequest.order() : 0);
        project.setIsFavorite(projectRequest.isFavorite() != null ? projectRequest.isFavorite() : false);
        project.setViewStyle(projectRequest.viewStyle() != null ? projectRequest.viewStyle() : ViewStyle.LIST);
        UUID planningCalendarId = projectRequest.planningCalendarId() != null
                ? projectRequest.planningCalendarId()
                : PlanningCalendarService.DEFAULT_ID;
        if (!planningCalendarRepository.existsById(planningCalendarId)) {
            throw new ResourceNotFoundException("Planning calendar not found: " + planningCalendarId);
        }
        project.setPlanningCalendarId(planningCalendarId);
        return projectRepository.save(project);
    }

    /**
     * Updates an existing project with non-null fields from the request.
     * Setting {@code clearParent} to true removes the parent relationship even when {@code parentId}
     * is also provided.
     *
     * @param projectId      the project UUID to update
     * @param projectRequest the update payload
     * @return the updated project entity
     */
    public Project update(UUID projectId, ProjectRequest projectRequest) {
        Project project = getOrThrow(projectId);
        if (projectRequest.name() != null) {
            project.setName(projectRequest.name());
        }
        if (projectRequest.color() != null) {
            project.setColor(projectRequest.color());
        }
        if (Boolean.TRUE.equals(projectRequest.clearParent())) {
            project.setParentId(null);
        } else if (projectRequest.parentId() != null) {
            project.setParentId(projectRequest.parentId());
        }
        if (projectRequest.order() != null) {
            project.setPosition(projectRequest.order());
        }
        if (projectRequest.isFavorite() != null) {
            project.setIsFavorite(projectRequest.isFavorite());
        }
        if (projectRequest.viewStyle() != null) {
            project.setViewStyle(projectRequest.viewStyle());
        }
        if (projectRequest.planningCalendarId() != null
                && !projectRequest.planningCalendarId().equals(project.getPlanningCalendarId())) {
            if (!planningCalendarRepository.existsById(projectRequest.planningCalendarId())) {
                throw new ResourceNotFoundException(
                        "Planning calendar not found: " + projectRequest.planningCalendarId());
            }
            boolean hasIncompatibleTask = taskRepository
                    .findByProjectIdAndScheduledAtIsNotNullAndIsCompletedFalseOrderByScheduledAtAsc(projectId)
                    .stream()
                    .anyMatch(task -> !planningCalendarService.allows(
                            projectRequest.planningCalendarId(), task.getScheduledAt(), task.isAllDay()));
            if (hasIncompatibleTask) {
                throw new IllegalArgumentException(
                        "Planning calendar does not allow an existing scheduled task");
            }
            project.setPlanningCalendarId(projectRequest.planningCalendarId());
        }
        return projectRepository.save(project);
    }

    /**
     * Bulk-updates the position of multiple projects in a single operation.
     * Projects not found in the repository are silently skipped.
     *
     * @param reorderRequests list of id/order pairs defining the new positions
     */
    public void reorder(List<ProjectReorderRequest> reorderRequests) {
        reorderRequests.forEach(reorderRequest ->
            projectRepository.findById(reorderRequest.id()).ifPresent(project -> {
                project.setPosition(reorderRequest.order());
                projectRepository.save(project);
            })
        );
    }

    /**
     * Deletes the project with the given ID. Throws {@link IllegalStateException} when attempting
     * to delete the inbox project, which must always exist.
     *
     * @param id the project UUID to delete
     */
    public void delete(UUID projectId) {
        Project project = getOrThrow(projectId);
        if (project.getIsInboxProject()) {
            throw new IllegalStateException("Cannot delete inbox project");
        }
        projectRepository.delete(project);
    }

    /**
     * Returns all incomplete tasks belonging to the given project, ordered by position.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if the project does not exist.
     *
     * @param id the project UUID
     * @return list of incomplete task entities in the project
     */
    @Transactional(readOnly = true)
    public List<Task> getProjectTasks(UUID projectId) {
        getOrThrow(projectId);
        return taskRepository.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
    }

    /**
     * Loads a project by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the project UUID
     * @return the project entity
     */
    private Project getOrThrow(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }
}
