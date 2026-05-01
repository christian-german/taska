package com.taska.domain.task;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public List<TaskDto> getAll(
            @RequestParam(required = false) UUID project_id,
            @RequestParam(required = false) UUID section_id,
            @RequestParam(required = false) String label,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false, defaultValue = "false") boolean show_completed) {
        return taskService.findAll(project_id, section_id, label, filter, show_completed);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@Valid @RequestBody TaskRequest req) {
        return taskService.create(req);
    }

    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable UUID id) {
        return taskService.findById(id);
    }

    @PutMapping("/{id}")
    public TaskDto update(@PathVariable UUID id, @RequestBody TaskRequest req) {
        return taskService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        taskService.delete(id);
    }

    @PostMapping("/{id}/close")
    public TaskDto close(@PathVariable UUID id) {
        return taskService.close(id);
    }

    @PostMapping("/{id}/reopen")
    public TaskDto reopen(@PathVariable UUID id) {
        return taskService.reopen(id);
    }

    @GetMapping("/{id}/subtasks")
    public List<TaskDto> getSubtasks(@PathVariable UUID id) {
        return taskService.getSubtasks(id);
    }
}
