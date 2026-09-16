package com.taska.domain.project.service;

import com.taska.domain.planningcalendar.repository.PlanningCalendarRepository;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.project.repository.Project;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
   * Returns the project with the given ID, or throws {@link
   * com.taska.exception.ResourceNotFoundException}.
   *
   * @param projectId the project UUID
   * @return the matching project entity
   */
  @Transactional(readOnly = true)
  public Project findById(UUID projectId) {
    return getOrThrow(projectId);
  }

  /**
   * Creates and persists a new project from the given request. Defaults: color "#808080", position
   * 0, not a favourite, view style LIST.
   *
   * @param projectCreateParameters application parameters for the new project
   * @return the persisted project entity
   */
  public Project create(ProjectCreateParameters projectCreateParameters) {
    Project project = new Project();
    project.setName(projectCreateParameters.name());
    project.setColor(projectCreateParameters.color());
    project.setParentId(projectCreateParameters.parentId());
    project.setPosition(projectCreateParameters.position());
    project.setIsFavorite(projectCreateParameters.favorite());
    project.setViewStyle(projectCreateParameters.viewStyle());
    UUID planningCalendarId =
        projectCreateParameters.planningCalendarId() != null
            ? projectCreateParameters.planningCalendarId()
            : PlanningCalendarService.DEFAULT_ID;
    if (!planningCalendarRepository.existsById(planningCalendarId)) {
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
  public Project update(UUID projectId, ProjectUpdateParameters projectUpdateParameters) {
    Project project = getOrThrow(projectId);
    project.setName(projectUpdateParameters.name());
    project.setColor(projectUpdateParameters.color());
    project.setParentId(projectUpdateParameters.parentId());
    project.setPosition(projectUpdateParameters.position());
    project.setIsFavorite(projectUpdateParameters.favorite());
    project.setViewStyle(projectUpdateParameters.viewStyle());
    if (!projectUpdateParameters.planningCalendarId().equals(project.getPlanningCalendarId())) {
      if (!planningCalendarRepository.existsById(projectUpdateParameters.planningCalendarId())) {
        throw new ResourceNotFoundException(
            "Planning calendar not found: " + projectUpdateParameters.planningCalendarId());
      }
      boolean hasIncompatibleTask =
          taskRepository
              .findByProjectIdAndScheduledAtIsNotNullAndIsCompletedFalseOrderByScheduledAtAsc(
                  projectId)
              .stream()
              .anyMatch(
                  task ->
                      !planningCalendarService.allows(
                          projectUpdateParameters.planningCalendarId(),
                          task.getScheduledAt(),
                          task.isAllDay()));
      if (hasIncompatibleTask) {
        throw new IllegalArgumentException(
            "Planning calendar does not allow an existing scheduled task");
      }
      project.setPlanningCalendarId(projectUpdateParameters.planningCalendarId());
    }
    return projectRepository.save(project);
  }

  /**
   * Bulk-updates the position of multiple projects in a single operation. Projects not found in the
   * repository are silently skipped.
   *
   * @param reorderParameters list of id/position pairs defining the new positions
   */
  public void reorder(List<ProjectReorderParameters> reorderParameters) {
    reorderParameters.forEach(
        reorderParameter ->
            projectRepository
                .findById(reorderParameter.projectId())
                .ifPresent(
                    project -> {
                      project.setPosition(reorderParameter.position());
                      projectRepository.save(project);
                    }));
  }

  /**
   * Deletes the project with the given ID. Throws {@link IllegalStateException} when attempting to
   * delete the inbox project, which must always exist.
   *
   * @param projectId the project UUID to delete
   */
  public void delete(UUID projectId) {
    Project project = getOrThrow(projectId);
    if (project.getIsInboxProject()) {
      throw new IllegalStateException("Cannot delete inbox project");
    }
    projectRepository.delete(project);
  }

  /**
   * Returns all incomplete tasks belonging to the given project, ordered by position. Throws {@link
   * com.taska.exception.ResourceNotFoundException} if the project does not exist.
   *
   * @param projectId the project UUID
   * @return list of incomplete task entities in the project
   */
  @Transactional(readOnly = true)
  public List<Task> getProjectTasks(UUID projectId) {
    getOrThrow(projectId);
    return taskRepository.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
  }

  /**
   * Loads a project by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not
   * found.
   *
   * @param projectId the project UUID
   * @return the project entity
   */
  private Project getOrThrow(UUID projectId) {
    return projectRepository
        .findById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
  }
}
