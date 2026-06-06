package com.taska.domain.timeentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    /** Returns all time entries whose start time falls within [from, to], ordered by start time. */
    List<TimeEntry> findByStartAtBetweenOrderByStartAtAsc(Instant from, Instant to);

    /** Returns all time entries for the given project whose start time falls within [from, to], ordered by start time. */
    List<TimeEntry> findByProjectIdAndStartAtBetweenOrderByStartAtAsc(UUID projectId, Instant from, Instant to);

    /** Deletes all time entries belonging to the given project. */
    void deleteByProjectId(UUID projectId);
}
