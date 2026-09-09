package com.taska.domain.project;

import com.taska.domain.task.TaskDto;
import com.taska.domain.task.TaskMapper;
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
     * @param projectRequest validated project creation payload
     * @return the created project DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectRequest projectRequest) {
        return projectMapper.toDto(projectService.create(projectRequest));
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
     * Updates an existing project with non-null fields from the request.
     *
     * @param projectId the project UUID
     * @param projectRequest the update payload
     * @return the updated project DTO
     */
    @PutMapping("/{projectId}")
    public ProjectDto update(@PathVariable UUID projectId, @RequestBody ProjectRequest projectRequest) {
        return projectMapper.toDto(projectService.update(projectId, projectRequest));
    }

    /**
     * Bulk-updates the position of multiple projects. Returns HTTP 204 on success.
     *
     * @param reorderRequests list of id/order pairs defining the new positions
     */
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody List<ProjectReorderRequest> reorderRequests) {
        projectService.reorder(reorderRequests);
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
