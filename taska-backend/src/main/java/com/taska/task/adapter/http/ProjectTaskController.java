package com.taska.task.adapter.http;

import com.taska.task.application.definition.TaskDefinitionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes a project's tasks.
 *
 * <p>
 * The resource is addressed under {@code /projects}, but what it returns is a task projection produced by the task module's mapper. It therefore
 * lives with the tasks it serialises: the project module would otherwise have to know {@code TaskDto} and {@code TaskMapper}, which is the only
 * reason it would depend on tasks at all.
 */
@RestController
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class ProjectTaskController {

    private final TaskDefinitionService taskService;
    private final TaskMapper taskMapper;

    /**
     * Returns all incomplete tasks belonging to the given project, ordered by their position.
     *
     * @param projectId the project UUID
     * @return list of task DTOs in the project
     */
    @GetMapping
    public List<TaskDto> getTasks(@PathVariable UUID projectId) {
        return taskService.findByProject(projectId).stream().map(taskMapper::toDto).toList();
    }
}
