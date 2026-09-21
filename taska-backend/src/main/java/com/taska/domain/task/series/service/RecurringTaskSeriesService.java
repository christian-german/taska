package com.taska.domain.task.series.service;

import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.definition.repository.TaskRepository;
import com.taska.domain.task.definition.service.TaskDefinitionService;
import com.taska.domain.task.occurrence.service.TaskOccurrenceService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Stops recurrence generation while preserving meaningful occurrence history. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecurringTaskSeriesService {

  private final TaskDefinitionService taskService;
  private final TaskOccurrenceService taskOccurrenceService;
  private final TaskRepository taskRepository;

  /** Stops the series before a generated occurrence, without creating a successor. */
  @Transactional
  public void truncateSeriesFrom(UUID seriesId, Instant occurrenceScheduledAt) {
    Task series = taskService.findById(seriesId);
    taskOccurrenceService.validateOccurrence(series, occurrenceScheduledAt);
    series.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    taskRepository.save(series);
    taskOccurrenceService.detachStatesFrom(seriesId, occurrenceScheduledAt);
  }
}
