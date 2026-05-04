package com.taska.domain.timeentry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    List<TimeEntry> findByStartAtBetweenOrderByStartAtAsc(LocalDateTime from, LocalDateTime to);

    List<TimeEntry> findByProjectIdAndStartAtBetweenOrderByStartAtAsc(UUID projectId, LocalDateTime from, LocalDateTime to);

    void deleteByProjectId(UUID projectId);
}
