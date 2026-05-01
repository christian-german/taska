package com.taska.domain.project;

import com.taska.domain.section.Section;
import com.taska.domain.section.SectionRepository;
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
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final SectionRepository sectionRepo;
    private final TaskRepository taskRepo;

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepo.findAllByOrderByPositionAsc();
    }

    @Transactional(readOnly = true)
    public Project findById(UUID id) {
        return getOrThrow(id);
    }

    public Project create(ProjectRequest req) {
        Project p = new Project();
        p.setName(req.name());
        p.setColor(req.color() != null ? req.color() : "charcoal");
        p.setParentId(req.parentId());
        p.setPosition(req.order() != null ? req.order() : 0);
        p.setIsFavorite(req.isFavorite() != null ? req.isFavorite() : false);
        p.setViewStyle(req.viewStyle() != null ? req.viewStyle() : ViewStyle.LIST);
        return projectRepo.save(p);
    }

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
        return projectRepo.save(p);
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
    public List<Task> getProjectTasks(UUID id) {
        getOrThrow(id);
        return taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(id);
    }

    @Transactional(readOnly = true)
    public List<Section> getProjectSections(UUID id) {
        getOrThrow(id);
        return sectionRepo.findByProjectIdOrderByPositionAsc(id);
    }

    private Project getOrThrow(UUID id) {
        return projectRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + id));
    }
}
