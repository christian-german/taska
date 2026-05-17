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
    private final TaskMapper taskMapper;

    @GetMapping
    public List<TaskDto> getAll(@RequestParam(required = false) UUID project_id,
                                @RequestParam(required = false) UUID section_id,
                                @RequestParam(required = false) String label,
                                @RequestParam(required = false) String filter,
                                @RequestParam(required = false, defaultValue = "false") boolean show_completed) {
        return taskService.findAll(project_id, section_id, label, filter, show_completed)
                .stream().map(taskMapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDto create(@Valid @RequestBody TaskRequest taskRequest) {
        return taskMapper.toDto(taskService.create(taskRequest));
    }

    @GetMapping("/{id}")
    public TaskDto getById(@PathVariable UUID id) {
        return taskMapper.toDto(taskService.findById(id));
    }

    @PutMapping("/{id}")
    public TaskDto update(@PathVariable UUID id, @RequestBody TaskRequest taskRequest) {
        return taskMapper.toDto(taskService.update(id, taskRequest));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID taskId) {
        taskService.delete(taskId);
    }

    @PostMapping("/{taskId}/close")
    public TaskDto close(@PathVariable UUID taskId) {
        return taskMapper.toDto(taskService.close(taskId));
    }

    @PostMapping("/{taskId}/reopen")
    public TaskDto reopen(@PathVariable UUID taskId) {
        return taskMapper.toDto(taskService.reopen(taskId));
    }

    @GetMapping("/{taskId}/subtasks")
    public List<TaskDto> getSubtasks(@PathVariable UUID taskId) {
        return taskService.getSubtasks(taskId).stream().map(taskMapper::toDto).toList();
    }
}
