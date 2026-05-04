package com.taska.domain.timeentry;

import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TimeEntryService {

    private final TimeEntryRepository repository;

    @Transactional(readOnly = true)
    public List<TimeEntry> findAll(UUID projectId, LocalDateTime from, LocalDateTime to) {
        if (projectId != null && from != null && to != null) {
            return repository.findByProjectIdAndStartAtBetweenOrderByStartAtAsc(projectId, from, to);
        }
        if (from != null && to != null) {
            return repository.findByStartAtBetweenOrderByStartAtAsc(from, to);
        }
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public TimeEntry findById(UUID id) {
        return getOrThrow(id);
    }

    public TimeEntry create(TimeEntryRequest req) {
        TimeEntry e = new TimeEntry();
        e.setStartAt(req.startAt());
        e.setEndAt(req.endAt());
        e.setProjectId(req.projectId());
        e.setDescription(req.description() != null ? req.description() : "");
        e.setNotes(req.notes());
        return repository.save(e);
    }

    public TimeEntry update(UUID id, TimeEntryRequest req) {
        TimeEntry e = getOrThrow(id);
        if (req.startAt() != null)    e.setStartAt(req.startAt());
        if (req.endAt() != null)      e.setEndAt(req.endAt());
        if (req.projectId() != null)  e.setProjectId(req.projectId());
        if (req.description() != null) e.setDescription(req.description());
        e.setNotes(req.notes()); // null is valid (clears notes)
        return repository.save(e);
    }

    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    private TimeEntry getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TimeEntry not found: " + id));
    }
}
