package com.taska.domain.timeentry;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

@RestController
@RequestMapping("/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService service;
    private final TimeEntryMapper mapper;

    /**
     * Returns time entries with optional project and date range filtering.
     * The {@code start} and {@code end} parameters accept flexible ISO datetime strings
     * (with or without timezone offset); naive local datetimes are treated as UTC.
     *
     * @param project_id optional project UUID to scope the result
     * @param start      optional range start as an ISO datetime string
     * @param end        optional range end as an ISO datetime string
     * @return list of time entry DTOs ordered by start time
     */
    @GetMapping
    public List<TimeEntryDto> getAll(
            @RequestParam(required = false) UUID project_id,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        Instant from = start != null ? parseLoose(start) : null;
        Instant to   = end   != null ? parseLoose(end)   : null;
        return service.findAll(project_id, from, to).stream().map(mapper::toDto).toList();
    }

    /**
     * Creates a new time entry. Returns HTTP 201 with the created time entry DTO.
     *
     * @param req the time entry creation payload
     * @return the created time entry DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TimeEntryDto create(@RequestBody TimeEntryRequest req) {
        return mapper.toDto(service.create(req));
    }

    /**
     * Returns a single time entry by its UUID.
     *
     * @param id the time entry UUID
     * @return the time entry DTO, or 404 if not found
     */
    @GetMapping("/{id}")
    public TimeEntryDto getById(@PathVariable UUID id) {
        return mapper.toDto(service.findById(id));
    }

    /**
     * Updates an existing time entry with non-null fields from the request.
     *
     * @param id  the time entry UUID
     * @param req the update payload
     * @return the updated time entry DTO
     */
    @PutMapping("/{id}")
    public TimeEntryDto update(@PathVariable UUID id, @RequestBody TimeEntryRequest req) {
        return mapper.toDto(service.update(id, req));
    }

    /**
     * Deletes the time entry with the given ID. Returns HTTP 204 on success.
     *
     * @param id the time entry UUID to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    /**
     * Parses an ISO datetime string leniently. Strings that include a timezone designator
     * ({@code Z}, {@code +}, or an offset after position 19) are parsed directly as an
     * {@link Instant}. Strings without a timezone are interpreted as UTC local datetime.
     *
     * @param s the datetime string to parse
     * @return the corresponding {@link Instant}
     */
    private static Instant parseLoose(String s) {
        if (s.endsWith("Z") || s.contains("+") || (s.length() > 19 && s.charAt(19) == '-')) {
            return Instant.parse(s);
        }
        return ISO_LOCAL_DATE_TIME.parse(s, java.time.LocalDateTime::from).toInstant(java.time.ZoneOffset.UTC);
    }
}
