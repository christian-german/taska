package com.taska.domain.timeentry;

import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository repository;

    /**
     * Returns time entries with optional project and date range filtering.
     * When both {@code from} and {@code to} are provided, only entries with a start time in that
     * range are included. When {@code projectId} is also provided, the result is further scoped
     * to that project. When no range is given, all entries are returned regardless of project.
     *
     * @param projectId optional project UUID to scope the result
     * @param from      optional range start (inclusive)
     * @param to        optional range end (exclusive)
     * @return list of matching time entry entities ordered by start time
     */
    @Transactional(readOnly = true)
    public List<TimeEntry> findAll(UUID projectId, Instant from, Instant to) {
        if (projectId != null && from != null && to != null) {
            return repository.findByProjectIdAndStartAtBetweenOrderByStartAtAsc(projectId, from, to);
        }
        if (from != null && to != null) {
            return repository.findByStartAtBetweenOrderByStartAtAsc(from, to);
        }
        return repository.findAll();
    }

    /**
     * Returns the time entry with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}.
     *
     * @param id the time entry UUID
     * @return the matching time entry entity
     */
    @Transactional(readOnly = true)
    public TimeEntry findById(UUID id) {
        return getOrThrow(id);
    }

    /**
     * Creates and persists a new time entry. Description defaults to an empty string when not provided.
     *
     * @param req the time entry creation payload
     * @return the persisted time entry entity
     */
    public TimeEntry create(TimeEntryRequest req) {
        TimeEntry e = new TimeEntry();
        e.setStartAt(req.startAt());
        e.setEndAt(req.endAt());
        e.setProjectId(req.projectId());
        e.setDescription(req.description() != null ? req.description() : "");
        e.setNotes(req.notes());
        return repository.save(e);
    }

    /**
     * Updates an existing time entry with non-null fields from the request.
     * The {@code notes} field is always applied (a null value clears the notes).
     *
     * @param id  the time entry UUID to update
     * @param req the update payload
     * @return the updated time entry entity
     */
    public TimeEntry update(UUID id, TimeEntryRequest req) {
        TimeEntry e = getOrThrow(id);
        if (req.startAt() != null)    e.setStartAt(req.startAt());
        if (req.endAt() != null)      e.setEndAt(req.endAt());
        if (req.projectId() != null)  e.setProjectId(req.projectId());
        if (req.description() != null) e.setDescription(req.description());
        e.setNotes(req.notes()); // null is valid (clears notes)
        return repository.save(e);
    }

    /**
     * Deletes the time entry with the given ID.
     * Throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the time entry UUID to delete
     */
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    /**
     * Loads a time entry by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param id the time entry UUID
     * @return the time entry entity
     */
    private TimeEntry getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry not found: " + id));
    }
}
