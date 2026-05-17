package com.taska.domain.timeentry;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService service;
    private final TimeEntryMapper mapper;

    @GetMapping
    public List<TimeEntryDto> getAll(
            @RequestParam(required = false) UUID project_id,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        Instant from = start != null ? Instant.parse(start) : null;
        Instant to   = end   != null ? Instant.parse(end)   : null;
        return service.findAll(project_id, from, to).stream().map(mapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryDto create(@RequestBody TimeEntryRequest req) {
        return mapper.toDto(service.create(req));
    }

    @GetMapping("/{id}")
    public TimeEntryDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.findById(id));
    }

    @PutMapping("/{id}")
    public TimeEntryDto update(@PathVariable UUID id, @RequestBody TimeEntryRequest req) {
        return mapper.toDto(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
