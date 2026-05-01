package com.taska.domain.project;

import com.taska.domain.section.SectionDto;
import com.taska.domain.task.TaskDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectDto> getAll() {
        return projectService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectRequest req) {
        return projectService.create(req);
    }

    @GetMapping("/{id}")
    public ProjectDto getById(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    @PutMapping("/{id}")
    public ProjectDto update(@PathVariable UUID id, @RequestBody ProjectRequest req) {
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
    public List<TaskDto> getTasks(@PathVariable UUID id) {
        return projectService.getProjectTasks(id);
    }

    @GetMapping("/{id}/sections")
    public List<SectionDto> getSections(@PathVariable UUID id) {
        return projectService.getProjectSections(id);
    }
}
