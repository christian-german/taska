package com.taska.domain.filter;

import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FilterService {

    private final FilterRepository filterRepo;
    private final TaskRepository taskRepo;

    /**
     * Returns all filters ordered by position ascending.
     *
     * @return list of all filter entities
     */
    @Transactional(readOnly = true)
    public List<Filter> findAll() {
        return filterRepo.findAllByOrderByPositionAsc();
    }

    /**
     * Returns the filter with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param id the filter UUID
     * @return the matching filter entity
     */
    @Transactional(readOnly = true)
    public Filter findById(UUID id) {
        return getOrThrow(id);
    }

    /**
     * Creates and persists a new filter. Defaults: color "charcoal", position 0, not a favourite.
     * The optional {@code projectId} and {@code hasDate} restrict which tasks the filter matches.
     *
     * @param req the filter creation payload
     * @return the persisted filter entity
     */
    public Filter create(FilterRequest req) {
        Filter f = new Filter();
        f.setName(req.name());
        f.setColor(req.color() != null ? req.color() : "charcoal");
        f.setPosition(req.order() != null ? req.order() : 0);
        f.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        f.setProjectId(req.projectId());
        f.setHasDate(req.hasDate());
        return filterRepo.save(f);
    }

    /**
     * Updates an existing filter with non-null fields from the request.
     * Setting {@code clearProject} to true removes the project constraint even when {@code projectId}
     * is also provided.
     *
     * @param id  the filter UUID to update
     * @param req the update payload
     * @return the updated filter entity
     */
    public Filter update(UUID id, FilterRequest req) {
        Filter f = getOrThrow(id);
        if (req.name() != null) f.setName(req.name());
        if (req.color() != null) f.setColor(req.color());
        if (req.order() != null) f.setPosition(req.order());
        if (req.isFavorite() != null) f.setIsFavorite(req.isFavorite());
        if (Boolean.TRUE.equals(req.clearProject())) f.setProjectId(null);
        else if (req.projectId() != null) f.setProjectId(req.projectId());
        if (req.hasDate() != null) f.setHasDate(req.hasDate());
        return filterRepo.save(f);
    }

    /**
     * Deletes the filter with the given ID.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the filter UUID to delete
     */
    public void delete(UUID id) {
        filterRepo.delete(getOrThrow(id));
    }

    /**
     * Returns the tasks matched by the given filter's criteria.
     * The selection logic depends on the filter's {@code projectId} and {@code hasDate} combination:
     * <ul>
     *   <li>Both set: tasks in the project with/without a due date, depending on {@code hasDate}.</li>
     *   <li>Only {@code projectId}: all incomplete tasks in the project.</li>
     *   <li>Only {@code hasDate}: all incomplete tasks globally, filtered by due-date presence.</li>
     *   <li>Neither set: all incomplete tasks.</li>
     * </ul>
     *
     * @param filterId the filter UUID
     * @return list of matching incomplete task entities
     */
    @Transactional(readOnly = true)
    public List<Task> getFilterTasks(UUID filterId) {
        Filter f = getOrThrow(filterId);
        UUID projectId = f.getProjectId();
        Boolean hasDate = f.getHasDate();

        if (projectId != null && hasDate != null) {
            return hasDate
                    ? taskRepo.findByProjectIdAndDueAtIsNotNullAndIsCompletedFalseOrderByDueAtAsc(projectId)
                    : taskRepo.findByProjectIdAndDueAtIsNullAndIsCompletedFalseOrderByPositionAsc(projectId);
        }
        if (projectId != null) {
            return taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
        }
        if (hasDate != null) {
            return hasDate
                    ? taskRepo.findAllWithDueAtNotCompleted()
                    : taskRepo.findAllWithNoDueAtNotCompleted();
        }
        return taskRepo.findAll().stream().filter(t -> !t.getIsCompleted()).toList();
    }

    /**
     * Loads a filter by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the filter UUID
     * @return the filter entity
     */
    private Filter getOrThrow(UUID id) {
        return filterRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filter not found: " + id));
    }
}
