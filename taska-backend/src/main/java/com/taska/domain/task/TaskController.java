package com.taska.domain.task;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getAll(
            @RequestParam(required = false) UUID project_id,
            @RequestParam(required = false) UUID section_id,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "false") boolean show_completed) {
        return taskService.findAll(project_id, section_id, label, filter, show_completed);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest req) {
        return taskService.create(req);
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable UUID id) {
        return taskService.findById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @RequestBody TaskRequest req) {
        return taskService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        taskService.delete(id);
    }

    @PostMapping("/{id}/close")
    public TaskResponse close(@PathVariable UUID id) {
        return taskService.close(id);
    }

    @PostMapping("/{id}/reopen")
    public TaskResponse reopen(@PathVariable UUID id) {
        return taskService.reopen(id);
    }

    @GetMapping("/{id}/subtasks")
    public List<TaskResponse> getSubtasks(@PathVariable UUID id) {
        return taskService.getSubtasks(id);
    }
}
