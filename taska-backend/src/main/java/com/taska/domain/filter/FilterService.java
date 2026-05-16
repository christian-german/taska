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

    @Transactional(readOnly = true)
    public List<Filter> findAll() {
        return filterRepo.findAllByOrderByPositionAsc();
    }

    @Transactional(readOnly = true)
    public Filter findById(UUID id) {
        return getOrThrow(id);
    }

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

    public void delete(UUID id) {
        filterRepo.delete(getOrThrow(id));
    }

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

    private Filter getOrThrow(UUID id) {
        return filterRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Filter not found: " + id));
    }
}
