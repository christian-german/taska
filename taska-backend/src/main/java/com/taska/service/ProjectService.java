package com.taska.service;

import com.taska.dto.ProjectReorderRequest;
import com.taska.dto.ProjectRequest;
import com.taska.dto.ProjectResponse;
import com.taska.dto.SectionResponse;
import com.taska.dto.TaskResponse;
import com.taska.exception.ResourceNotFoundException;
import com.taska.model.Project;
import com.taska.model.ViewStyle;
import com.taska.repository.ProjectRepository;
import com.taska.repository.SectionRepository;
import com.taska.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final SectionRepository sectionRepo;
    private final TaskRepository taskRepo;
    private final SectionService sectionService;
    private final TaskService taskService;

    public ProjectService(ProjectRepository projectRepo, SectionRepository sectionRepo,
                          TaskRepository taskRepo, SectionService sectionService, TaskService taskService) {
        this.projectRepo = projectRepo;
        this.sectionRepo = sectionRepo;
        this.taskRepo = taskRepo;
        this.sectionService = sectionService;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepo.findAllByOrderByPositionAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public ProjectResponse create(ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name());
        p.setColor(req.color() != null ? req.color() : "charcoal");
        p.setParentId(req.parentId());
        p.setPosition(req.order() != null ? req.order() : 0);
        p.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        p.setViewStyle(req.viewStyle() != null ? req.viewStyle() : ViewStyle.LIST);
        return toResponse(projectRepo.save(p));
    }

    public ProjectResponse update(UUID id, ProjectRequest req) {
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
    public List<TaskResponse> getProjectTasks(UUID id) {
        getOrThrow(id);
        return taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(id)
                .stream().map(taskService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> getProjectSections(UUID id) {
        getOrThrow(id);
        return sectionRepo.findByProjectIdOrderByPositionAsc(id)
                .stream().map(sectionService::toResponse).toList();
    }

    private Project getOrThrow(UUID id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }

    public ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getColor(), p.getParentId(),
                p.getPosition(), p.getIsFavorite(), p.getViewStyle(), p.getIsInboxProject(),
                p.getCreatedAt(), p.getUpdatedAt());
    }
}
