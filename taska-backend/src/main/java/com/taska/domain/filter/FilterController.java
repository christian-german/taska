package com.taska.domain.filter;

import com.taska.domain.task.TaskDto;
import com.taska.domain.task.TaskMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/filters")
@RequiredArgsConstructor
public class FilterController {

    private final FilterService filterService;
    private final FilterMapper filterMapper;
    private final TaskMapper taskMapper;

    @GetMapping
    public List<FilterDto> getAll() {
        return filterService.findAll().stream().map(filterMapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public FilterDto getById(@PathVariable UUID id) {
        return filterMapper.toDto(filterService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilterDto create(@Valid @RequestBody FilterRequest req) {
        return filterMapper.toDto(filterService.create(req));
    }

    @PutMapping("/{id}")
    public FilterDto update(@PathVariable UUID id, @RequestBody FilterRequest req) {
        return filterMapper.toDto(filterService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        filterService.delete(id);
    }

    @GetMapping("/{id}/tasks")
    public List<TaskDto> getTasks(@PathVariable UUID id) {
        return filterService.getFilterTasks(id).stream().map(taskMapper::toDto).toList();
    }
}
