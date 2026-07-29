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

    /**
     * Returns all filters ordered by position.
     *
     * @return list of all filter DTOs
     */
    @GetMapping
    public List<FilterDto> getAll() {
        return filterService.findAll().stream().map(filterMapper::toDto).toList();
    }

    /**
     * Returns a single filter by its UUID.
     *
     * @param id the filter UUID
     * @return the filter DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public FilterDto getById(@PathVariable UUID id) {
        return filterMapper.toDto(filterService.findById(id));
    }

    /**
     * Creates a new filter. Returns HTTP 201 with the created filter DTO.
     *
     * @param req validated filter creation payload
     * @return the created filter DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilterDto create(@Valid @RequestBody FilterRequest req) {
        return filterMapper.toDto(filterService.create(req));
    }

    /**
     * Updates an existing filter with non-null fields from the request.
     *
     * @param id  the filter UUID
     * @param req the update payload
     * @return the updated filter DTO
     */
    @PutMapping("/{id}")
    public FilterDto update(@PathVariable UUID id, @RequestBody FilterRequest req) {
        return filterMapper.toDto(filterService.update(id, req));
    }

    /**
     * Deletes the filter with the given ID. Returns HTTP 204 on success.
     *
     * @param id the filter UUID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        filterService.delete(id);
    }

    /**
     * Returns the tasks matched by the given filter's criteria (project and/or hasDate combination).
     *
     * @param id the filter UUID
     * @return list of matching task DTOs
     */
    @GetMapping("/{id}/tasks")
    public List<TaskDto> getTasks(@PathVariable UUID id) {
        return filterService.getFilterTasks(id).stream().map(taskMapper::toDto).toList();
    }
}
