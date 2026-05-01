package com.taska.domain.filter;

import com.taska.domain.task.TaskDto;
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

    @GetMapping
    public List<FilterDto> getAll() {
        return filterService.findAll();
    }

    @GetMapping("/{id}")
    public FilterDto getById(@PathVariable UUID id) {
        return filterService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilterDto create(@Valid @RequestBody FilterRequest req) {
        return filterService.create(req);
    }

    @PutMapping("/{id}")
    public FilterDto update(@PathVariable UUID id, @RequestBody FilterRequest req) {
        return filterService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        filterService.delete(id);
    }

    @GetMapping("/{id}/tasks")
    public List<TaskDto> getTasks(@PathVariable UUID id) {
        return filterService.getFilterTasks(id);
    }
}
