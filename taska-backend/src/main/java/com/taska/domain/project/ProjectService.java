package com.taska.domain.project;

import com.taska.domain.section.SectionRepository;
import com.taska.domain.section.SectionDto;
import com.taska.domain.section.SectionService;
import com.taska.domain.task.TaskRepository;
import com.taska.domain.task.TaskDto;
import com.taska.domain.task.TaskService;
import com.taska.exception.ResourceNotFoundException;
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
    private final SectionService sectionService;
    private final TaskService taskService;

    @Transactional(readOnly = true)
    public List<ProjectDto> findAll() {
        return projectRepo.findAllByOrderByPositionAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectDto findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public ProjectDto create(ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name());
        p.setColor(req.color() != null ? req.color() : "charcoal");
        p.setParentId(req.parentId());
        p.setPosition(req.order() != null ? req.order() : 0);
        p.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        p.setViewStyle(req.viewStyle() != null ? req.viewStyle() : ViewStyle.LIST);
        return toResponse(projectRepo.save(p));
    }

    public ProjectDto update(UUID id, ProjectRequest req) {
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
        return toResponse(projectRepo.save(p));
    }

    public void reorder(List<ProjectReorderRequest> items) {
        items.forEach(item ->
            projectRepo.findById(item.id()).ifPresent(p -> {
                p.setPosition(item.order());
                projectRepo.save(p);
            })
        );
    }

    public void delete(UUID id) {
        Project p = getOrThrow(id);
        if (p.getIsInboxProject()) {
            throw new IllegalStateException("Cannot delete inbox project");
        }
        projectRepo.delete(p);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getProjectTasks(UUID id) {
        getOrThrow(id);
        return taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(id)
                .stream().map(taskService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SectionDto> getProjectSections(UUID id) {
        getOrThrow(id);
        return sectionRepo.findByProjectIdOrderByPositionAsc(id)
                .stream().map(sectionService::toResponse).toList();
    }

    private Project getOrThrow(UUID id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public ProjectDto toResponse(Project p) {
        return new ProjectDto(p.getId(), p.getName(), p.getColor(), p.getParentId(),
                p.getPosition(), p.getIsFavorite(), p.getViewStyle(), p.getIsInboxProject(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
