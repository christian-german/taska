package com.taska.domain.timeentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByStartAtBetweenOrderByStartAtAsc(Instant from, Instant to);

    List<TimeEntry> findByProjectIdAndStartAtBetweenOrderByStartAtAsc(UUID projectId, Instant from, Instant to);

    void deleteByProjectId(UUID projectId);
}
