package com.taska.domain.filter;

import com.taska.domain.task.TaskRepository;
import com.taska.domain.task.TaskResponse;
import com.taska.domain.task.TaskService;
import com.taska.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FilterService {

    private final FilterRepository filterRepo;
    private final TaskRepository taskRepo;
    private final TaskService taskService;

    public FilterService(FilterRepository filterRepo, TaskRepository taskRepo, TaskService taskService) {
        this.filterRepo = filterRepo;
        this.taskRepo = taskRepo;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public List<FilterResponse> findAll() {
        return filterRepo.findAllByOrderByPositionAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FilterResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public FilterResponse create(FilterRequest req) {
        Filter f = new Filter();
        f.setName(req.name());
        f.setColor(req.color() != null ? req.color() : "charcoal");
        f.setPosition(req.order() != null ? req.order() : 0);
        f.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        f.setProjectId(req.projectId());
        f.setHasDate(req.hasDate());
        return toResponse(filterRepo.save(f));
    }

    public FilterResponse update(UUID id, FilterRequest req) {
        Filter f = getOrThrow(id);
        if (req.name() != null) f.setName(req.name());
        if (req.color() != null) f.setColor(req.color());
        if (req.order() != null) f.setPosition(req.order());
        if (req.isFavorite() != null) f.setIsFavorite(req.isFavorite());
        if (Boolean.TRUE.equals(req.clearProject())) f.setProjectId(null);
        else if (req.projectId() != null) f.setProjectId(req.projectId());
        if (req.hasDate() != null) f.setHasDate(req.hasDate());
        return toResponse(filterRepo.save(f));
    }

    public void delete(UUID id) {
        filterRepo.delete(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getFilterTasks(UUID filterId) {
        Filter f = getOrThrow(filterId);
        UUID projectId = f.getProjectId();
        Boolean hasDate = f.getHasDate();

        if (projectId != null && hasDate != null) {
            var tasks = hasDate
                    ? taskRepo.findByProjectIdAndDueDateIsNotNullAndIsCompletedFalseOrderByDueDateAsc(projectId)
                    : taskRepo.findByProjectIdAndDueDateIsNullAndIsCompletedFalseOrderByPositionAsc(projectId);
            return tasks.stream().map(taskService::toResponse).toList();
        }
        if (projectId != null) {
            return taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId)
                    .stream().map(taskService::toResponse).toList();
        }
        if (hasDate != null) {
            var tasks = hasDate
                    ? taskRepo.findAllWithDueDateNotCompleted()
                    : taskRepo.findAllWithNoDueDateNotCompleted();
            return tasks.stream().map(taskService::toResponse).toList();
        }
        return taskRepo.findAll().stream()
                .filter(t -> !t.getIsCompleted())
                .map(taskService::toResponse).toList();
    }

    private Filter getOrThrow(UUID id) {
        return filterRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filter not found: " + id));
    }

    public FilterResponse toResponse(Filter f) {
        return new FilterResponse(
                f.getId(), f.getName(), f.getColor(),
                f.getPosition(), f.getIsFavorite(),
                f.getProjectId(), f.getHasDate()
        );
    }
}
