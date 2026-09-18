package com.taska.domain.task.service;

import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Owns changes that split or truncate a recurring task series. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecurringTaskSeriesService {

  private final TaskService taskService;
  private final TaskOccurrenceService taskOccurrenceService;
  private final TaskRepository taskRepository;
  private final TaskPriorityEvaluationRepository priorityEvaluationRepository;

  /**
   * Truncates a recurring series and creates a successor series with a partial update.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt first occurrence represented by the successor series
   * @param parameters fields to apply to the successor series
   * @param priorityProvided whether the caller explicitly supplied the priority field
   * @return the newly created successor series
   */
  @Transactional
  public TaskResult updateSeriesFrom(
      UUID seriesId,
      Instant occurrenceScheduledAt,
      TaskPatchParameters parameters,
      boolean priorityProvided) {
    if (occurrenceScheduledAt == null) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required when scope is provided");
    }
    TaskDefinitionRules.assertDueAtAllowed(true, parameters.dueAt());
    Task series = taskService.findById(seriesId);
    series.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    taskRepository.save(series);

    Task successor = new Task();
    successor.setContent(parameters.content() != null ? parameters.content() : series.getContent());
    successor.setType(parameters.type() != null ? parameters.type() : series.getType());
    successor.setDescription(
        parameters.description() != null ? parameters.description() : series.getDescription());
    successor.setProjectId(series.getProjectId());
    successor.setParentId(series.getParentId());
    successor.setPosition(series.getPosition());
    successor.setPriority(priorityProvided ? parameters.priority() : series.getPriority());
    successor.setLabels(parameters.labels() != null ? parameters.labels() : series.getLabels());
    successor.setScheduledAt(occurrenceScheduledAt);
    successor.setDueAt(null);
    successor.setAllDay(series.isAllDay());
    successor.setIsRecurring(true);
    successor.setEstimateMinutes(
        parameters.estimateMinutes() != null
            ? parameters.estimateMinutes()
            : series.getEstimateMinutes());
    successor.setRecurrenceRule(
        parameters.recurrenceRule() != null
            ? parameters.recurrenceRule()
            : series.getRecurrenceRule());
    return TaskResult.base(taskRepository.save(successor));
  }

  /**
   * Splits a recurring series and creates its following replacement from a complete request.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt first occurrence represented by the replacement series
   * @param parameters complete replacement values
   * @return the replacement series
   */
  @Transactional
  public TaskResult replaceFollowing(
      UUID seriesId, Instant occurrenceScheduledAt, TaskUpdateParameters parameters) {
    if (!parameters.recurring()) {
      throw new IllegalArgumentException("Following-series replacement must remain recurring");
    }
    TaskDefinitionRules.assertDueAtAllowed(true, parameters.dueAt());
    Task original = taskService.findById(seriesId);
    if (!Boolean.TRUE.equals(original.getIsRecurring())) {
      throw new IllegalArgumentException("Following-series replacement requires a recurring task");
    }
    taskOccurrenceService.validateOccurrence(original, occurrenceScheduledAt);
    original.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    taskRepository.save(original);

    Task replacement = new Task();
    // Reuse the same complete-replacement mapping and planning-calendar validation as base tasks.
    taskService.replaceMutableFields(replacement, parameters);
    Task saved = taskRepository.save(replacement);
    priorityEvaluationRepository.deleteByTaskId(seriesId);
    return TaskResult.base(saved);
  }

  /**
   * Truncates a recurring series immediately before the identified occurrence.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt first occurrence excluded from the series
   */
  @Transactional
  public void truncateSeriesFrom(UUID seriesId, Instant occurrenceScheduledAt) {
    Task series = taskService.findById(seriesId);
    series.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    taskRepository.save(series);
  }
}
