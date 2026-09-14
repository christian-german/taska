package com.taska.domain.project.controller;

import com.taska.domain.task.controller.TaskDto;
import com.taska.domain.task.controller.TaskMapper;
import com.taska.domain.project.service.ProjectCreateParameters;
import com.taska.domain.project.service.ProjectReorderParameters;
import com.taska.domain.project.service.ProjectService;
import com.taska.domain.project.service.ProjectUpdateParameters;
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
    private final ProjectMapper projectMapper;
    private final TaskMapper taskMapper;

    /**
     * Returns all projects ordered by position.
     *
     * @return list of all project DTOs
     */
    @GetMapping
    public List<ProjectDto> getAll() {
        return projectService.findAll().stream().map(projectMapper::toDto).toList();
    }

    /**
     * Creates a new project. Returns HTTP 201 with the created project DTO.
     *
     * @param projectCreateRequest validated project creation payload
     * @return the created project DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectCreateRequest projectCreateRequest) {
        ProjectCreateParameters projectCreateParameters = projectMapper.toParameters(projectCreateRequest);
        return projectMapper.toDto(projectService.create(projectCreateParameters));
    }

    /**
     * Returns a single project by its UUID.
     *
     * @param projectId the project UUID
     * @return the project DTO, or 404 if not found
     */
    @GetMapping("/{projectId}")
    public ProjectDto getById(@PathVariable UUID projectId) {
        return projectMapper.toDto(projectService.findById(projectId));
    }

    /**
     * Replaces all mutable fields of an existing project.
     *
     * @param projectId the project UUID
     * @param projectUpdateRequest the complete replacement payload
     * @return the updated project DTO
     */
    @PutMapping("/{projectId}")
    public ProjectDto update(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest projectUpdateRequest) {
        ProjectUpdateParameters projectUpdateParameters = projectMapper.toParameters(projectUpdateRequest);
        return projectMapper.toDto(projectService.update(projectId, projectUpdateParameters));
    }

    /**
     * Bulk-updates the position of multiple projects. Returns HTTP 204 on success.
     *
     * @param reorderRequests list of id/order pairs defining the new positions
     */
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody List<ProjectReorderRequest> reorderRequests) {
        List<ProjectReorderParameters> reorderParameters = reorderRequests.stream()
                .map(projectMapper::toParameters)
                .toList();
        projectService.reorder(reorderParameters);
    }

    /**
     * Deletes the project with the given ID. Returns HTTP 204 on success.
     * Deleting the inbox project is not allowed and will result in an error.
     *
     * @param projectId the project UUID to delete
     */
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID projectId) {
        projectService.delete(projectId);
    }

    /**
     * Returns all incomplete tasks belonging to the given project, ordered by position.
     *
     * @param projectId the project UUID
     * @return list of task DTOs in the project
     */
    @GetMapping("/{projectId}/tasks")
    public List<TaskDto> getTasks(@PathVariable UUID projectId) {
        return projectService.getProjectTasks(projectId).stream().map(taskMapper::toDto).toList();
    }
}
