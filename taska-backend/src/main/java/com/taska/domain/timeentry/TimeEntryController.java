package com.taska.domain.timeentry;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return service.findAll(project_id, start, end).stream().map(mapper::toDto).toList();
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
