package com.taska.domain.filter;

import com.taska.domain.task.TaskResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/filters")
public class FilterController {

    private final FilterService filterService;

    public FilterController(FilterService filterService) {
        this.filterService = filterService;
    }

    @GetMapping
    public List<FilterResponse> getAll() {
        return filterService.findAll();
    }

    @GetMapping("/{id}")
    public FilterResponse getById(@PathVariable UUID id) {
        return filterService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilterResponse create(@Valid @RequestBody FilterRequest req) {
        return filterService.create(req);
    }

    @PutMapping("/{id}")
    public FilterResponse update(@PathVariable UUID id, @RequestBody FilterRequest req) {
        return filterService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        filterService.delete(id);
    }

    @GetMapping("/{id}/tasks")
    public List<TaskResponse> getTasks(@PathVariable UUID id) {
        return filterService.getFilterTasks(id);
    }
}
