package com.taska.controller;

import com.taska.dto.ProjectReorderRequest;
import com.taska.dto.ProjectRequest;
import com.taska.dto.ProjectResponse;
import com.taska.dto.SectionResponse;
import com.taska.dto.TaskResponse;
import com.taska.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> getAll() {
        return projectService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(@Valid @RequestBody ProjectRequest req) {
        return projectService.create(req);
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable UUID id, @RequestBody ProjectRequest req) {
        return projectService.update(id, req);
    }

    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody List<ProjectReorderRequest> items) {
        projectService.reorder(items);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        projectService.delete(id);
    }

    @GetMapping("/{id}/tasks")
    public List<TaskResponse> getTasks(@PathVariable UUID id) {
        return projectService.getProjectTasks(id);
    }

    @GetMapping("/{id}/sections")
    public List<SectionResponse> getSections(@PathVariable UUID id) {
        return projectService.getProjectSections(id);
    }
}
