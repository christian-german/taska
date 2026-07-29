package com.taska.domain.project;

import com.taska.domain.section.SectionDto;
import com.taska.domain.section.SectionMapper;
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
    private final SectionMapper sectionMapper;

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
     * @param req validated project creation payload
     * @return the created project DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDto create(@Valid @RequestBody ProjectRequest req) {
        return projectMapper.toDto(projectService.create(req));
    }

    /**
     * Returns a single project by its UUID.
     *
     * @param id the project UUID
     * @return the project DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public ProjectDto getById(@PathVariable UUID id) {
        return projectMapper.toDto(projectService.findById(id));
    }

    /**
     * Updates an existing project with non-null fields from the request.
     *
     * @param id  the project UUID
     * @param req the update payload
     * @return the updated project DTO
     */
    @PutMapping("/{id}")
    public ProjectDto update(@PathVariable UUID id, @RequestBody ProjectRequest req) {
        return projectMapper.toDto(projectService.update(id, req));
    }

    /**
     * Bulk-updates the position of multiple projects. Returns HTTP 204 on success.
     *
     * @param items list of id/order pairs defining the new positions
     */
    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody List<ProjectReorderRequest> items) {
        projectService.reorder(items);
    }

    /**
     * Deletes the project with the given ID. Returns HTTP 204 on success.
     * Deleting the inbox project is not allowed and will result in an error.
     *
     * @param id the project UUID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        projectService.delete(id);
    }

    /**
     * Returns all incomplete tasks belonging to the given project, ordered by position.
     *
     * @param id the project UUID
     * @return list of task DTOs in the project
     */
    @GetMapping("/{id}/tasks")
    public List<TaskDto> getTasks(@PathVariable UUID id) {
        return projectService.getProjectTasks(id).stream().map(taskMapper::toDto).toList();
    }

    /**
     * Returns all sections belonging to the given project, ordered by position.
     *
     * @param id the project UUID
     * @return list of section DTOs in the project
     */
    @GetMapping("/{id}/sections")
    public List<SectionDto> getSections(@PathVariable UUID id) {
        return projectService.getProjectSections(id).stream().map(sectionMapper::toDto).toList();
    }
}
