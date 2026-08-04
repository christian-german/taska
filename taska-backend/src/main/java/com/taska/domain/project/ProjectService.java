package com.taska.domain.project;

import com.taska.domain.section.Section;
import com.taska.domain.section.SectionRepository;
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

    private final ProjectRepository projectRepo;
    private final SectionRepository sectionRepo;
    private final TaskRepository taskRepo;
    private final PlanningCalendarRepository calendarRepo;
    private final PlanningCalendarService calendarService;

    /**
     * Returns all projects ordered by their position ascending.
     *
     * @return list of all project entities
     */
    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepo.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the project with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param id the project UUID
     * @return the matching project entity
     */
    @Transactional(readOnly = true)
    public Project findById(UUID id) {
        return getOrThrow(id);
    }

    /**
     * Creates and persists a new project from the given request.
     * Defaults: color "#808080", position 0, not a favourite, view style LIST.
     *
     * @param req the project creation payload
     * @return the persisted project entity
     */
    public Project create(ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name());
        p.setColor(req.color() != null ? req.color() : "#808080");
        p.setParentId(req.parentId());
        p.setPosition(req.order() != null ? req.order() : 0);
        p.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        p.setViewStyle(req.viewStyle() != null ? req.viewStyle() : ViewStyle.LIST);
        UUID calendarId = req.planningCalendarId() != null ? req.planningCalendarId() : PlanningCalendarService.DEFAULT_ID;
        if (!calendarRepo.existsById(calendarId)) throw new ResourceNotFoundException("Planning calendar not found: " + calendarId);
        p.setPlanningCalendarId(calendarId);
        return projectRepo.save(p);
    }

    /**
     * Updates an existing project with non-null fields from the request.
     * Setting {@code clearParent} to true removes the parent relationship even when {@code parentId}
     * is also provided.
     *
     * @param id  the project UUID to update
     * @param req the update payload
     * @return the updated project entity
     */
    public Project update(UUID id, ProjectRequest req) {
        Project p = getOrThrow(id);
        if (req.name() != null) p.setName(req.name());
        if (req.color() != null) p.setColor(req.color());
        if (Boolean.TRUE.equals(req.clearParent())) {
            p.setParentId(null);
        } else if (req.parentId() != null) {
            p.setParentId(req.parentId());
        }
        if (req.order() != null) p.setPosition(req.order());
        if (req.isFavorite() != null) p.setIsFavorite(req.isFavorite());
        if (req.viewStyle() != null) p.setViewStyle(req.viewStyle());
        if (req.planningCalendarId() != null && !req.planningCalendarId().equals(p.getPlanningCalendarId())) {
            if (!calendarRepo.existsById(req.planningCalendarId())) throw new ResourceNotFoundException("Planning calendar not found: " + req.planningCalendarId());
            boolean incompatible = taskRepo.findByProjectIdAndScheduledAtIsNotNullAndIsCompletedFalseOrderByScheduledAtAsc(id).stream()
                    .anyMatch(t -> !calendarService.allows(req.planningCalendarId(), t.getScheduledAt(), t.isAllDay()));
            if (incompatible) throw new IllegalArgumentException("Planning calendar does not allow an existing scheduled task");
            p.setPlanningCalendarId(req.planningCalendarId());
        }
        return projectRepo.save(p);
    }

    /**
     * Bulk-updates the position of multiple projects in a single operation.
     * Projects not found in the repository are silently skipped.
     *
     * @param items list of id/order pairs defining the new positions
     */
    public void reorder(List<ProjectReorderRequest> items) {
        items.forEach(item ->
            projectRepo.findById(item.id()).ifPresent(p -> {
                p.setPosition(item.order());
                projectRepo.save(p);
            })
        );
    }

    /**
     * Deletes the project with the given ID. Throws {@link IllegalStateException} when attempting
     * to delete the inbox project, which must always exist.
     *
     * @param id the project UUID to delete
     */
    public void delete(UUID id) {
        Project p = getOrThrow(id);
        if (p.getIsInboxProject()) {
            throw new IllegalStateException("Cannot delete inbox project");
        }
        projectRepo.delete(p);
    }

    /**
     * Returns all incomplete tasks belonging to the given project, ordered by position.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if the project does not exist.
     *
     * @param id the project UUID
     * @return list of incomplete task entities in the project
     */
    @Transactional(readOnly = true)
    public List<Task> getProjectTasks(UUID id) {
        getOrThrow(id);
        return taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(id);
    }

    /**
     * Returns all sections belonging to the given project, ordered by position.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if the project does not exist.
     *
     * @param id the project UUID
     * @return list of section entities in the project
     */
    @Transactional(readOnly = true)
    public List<Section> getProjectSections(UUID id) {
        getOrThrow(id);
        return sectionRepo.findByProjectIdOrderByPositionAsc(id);
    }

    /**
     * Loads a project by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the project UUID
     * @return the project entity
     */
    private Project getOrThrow(UUID id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
