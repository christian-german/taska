package com.taska.task.application.occurrence;

import com.taska.task.application.definition.TaskDefinitionService;
import com.taska.task.model.Task;
import com.taska.task.persistence.TaskRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stops recurrence generation while preserving meaningful occurrence history.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecurringTaskSeriesService {

    private final TaskDefinitionService taskService;
    private final TaskOccurrenceService taskOccurrenceService;
    private final TaskRepository taskRepository;

    /**
     * Stops the series before a generated occurrence, without creating a successor.
     */
    @Transactional
    public void truncateSeriesFrom(UUID seriesId, Instant occurrenceScheduledAt) {
        Task series = taskService.findById(seriesId);
        taskOccurrenceService.validateOccurrence(series, occurrenceScheduledAt);
        series.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
        taskRepository.save(series);
        taskOccurrenceService.detachStatesFrom(seriesId, occurrenceScheduledAt);
    }
}
