package com.taska.domain.task.occurrence.service;

import com.taska.config.TaskaProperties;
import com.taska.domain.notification.service.TaskOccurrenceNotificationService;
import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.definition.repository.TaskRepository;
import com.taska.domain.task.definition.service.TaskDefinitionService;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceState;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the lifecycle and representation of generated occurrences from recurring task series.
 *
 * <p>An occurrence has no independent task row. Its stable identity is the series identifier plus
 * its RRULE-generated schedule, while {@link TaskOccurrenceState} stores only exceptional state
 * such as completion, skipping, or field overrides.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskOccurrenceService {

  private final TaskDefinitionService taskService;
  private final TaskRepository taskRepository;
  private final TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  private final TaskRecurrenceService taskRecurrenceService;
  private final TaskaProperties taskaProperties;
  private final TaskOccurrenceNotificationService taskOccurrenceNotificationService;

  /**
   * Returns all regular tasks and recurring occurrences inside the requested date range.
   *
   * <p>Generated occurrences are merged with their persisted exceptional state. Skipped occurrences
   * and occurrences rescheduled outside the range are excluded, while occurrences rescheduled into
   * the range are included.
   *
   * @param from start of the date range, inclusive
   * @param to end of the date range, inclusive
   * @param showCompleted whether completed non-recurring tasks are included
   * @return task results representing regular tasks and recurring occurrences
   */
  public List<TaskResult> findOccurrencesForDateRange(
      LocalDate from, LocalDate to, boolean showCompleted) {
    ZoneId calendarZone = taskaProperties.getCalendar().getTimeZone();
    Instant periodStart = from.atStartOfDay(calendarZone).toInstant();
    Instant periodEnd = to.plusDays(1).atStartOfDay(calendarZone).toInstant();

    List<Task> nonRecurring =
        showCompleted
            ? taskRepository.findNonRecurringTasksIncludingCompletedInPeriod(periodStart, periodEnd)
            : taskRepository.findNonRecurringTasksInPeriod(periodStart, periodEnd);
    List<TaskResult> taskResults =
        new ArrayList<>(nonRecurring.stream().map(TaskResult::base).toList());

    // Detached states outlive the stretch of time their series generates, so they are collected by
    // their own date instead of through the list of series still active over the period.
    List<TaskOccurrenceState> detachedStates =
        taskOccurrenceStateRepository.findDetachedInPeriod(periodStart, periodEnd);
    if (!detachedStates.isEmpty()) {
      Map<UUID, Task> detachedSeriesById =
          taskRepository
              .findAllById(
                  detachedStates.stream().map(TaskOccurrenceState::getSeriesId).distinct().toList())
              .stream()
              .collect(Collectors.toMap(Task::getId, series -> series));
      for (TaskOccurrenceState detachedState : detachedStates) {
        Task series = detachedSeriesById.get(detachedState.getSeriesId());
        if (series != null && Boolean.TRUE.equals(series.getIsRecurring())) {
          taskResults.add(
              TaskResult.occurrence(
                  series, detachedState, detachedState.getOccurrenceScheduledAt()));
        }
      }
    }

    List<Task> recurringSeries =
        taskRepository.findActiveRecurringTasksForPeriod(periodStart, periodEnd);
    if (recurringSeries.isEmpty()) {
      return taskResults;
    }

    List<UUID> recurringSeriesIds = recurringSeries.stream().map(Task::getId).toList();

    // States identified inside the range overlay the matching generated occurrences.
    Map<UUID, Map<Instant, TaskOccurrenceState>> statesBySeries =
        taskOccurrenceStateRepository
            .findBySeriesIdInAndOccurrenceScheduledAtBetween(
                recurringSeriesIds, periodStart, periodEnd)
            .stream()
            .collect(
                Collectors.groupingBy(
                    TaskOccurrenceState::getSeriesId,
                    Collectors.toMap(
                        TaskOccurrenceState::getOccurrenceScheduledAt,
                        occurrenceState -> occurrenceState,
                        (existingState, _) -> existingState)));

    // Modified occurrences can move into the range although their stable identity is outside it.
    Map<UUID, List<TaskOccurrenceState>> movedInBySeries =
        taskOccurrenceStateRepository
            .findBySeriesIdInAndStatusAndScheduledAtBetween(
                recurringSeriesIds, TaskOccurrenceStatus.MODIFIED, periodStart, periodEnd)
            .stream()
            .filter(
                occurrenceState ->
                    occurrenceState.getOccurrenceScheduledAt().isBefore(periodStart)
                        || !occurrenceState.getOccurrenceScheduledAt().isBefore(periodEnd))
            .collect(Collectors.groupingBy(TaskOccurrenceState::getSeriesId));

    for (Task series : recurringSeries) {
      Map<Instant, TaskOccurrenceState> occurrenceStates =
          statesBySeries.getOrDefault(series.getId(), Map.of());
      List<Instant> occurrences =
          taskRecurrenceService.getOccurrencesInRange(series, periodStart, periodEnd);

      for (Instant occurrenceScheduledAt : occurrences) {
        TaskOccurrenceState occurrenceState = occurrenceStates.get(occurrenceScheduledAt);
        if (occurrenceState != null
            && occurrenceState.getStatus() == TaskOccurrenceStatus.SKIPPED) {
          continue;
        }
        // A generated occurrence rescheduled outside this range must not remain on its old day.
        if (occurrenceState != null
            && occurrenceState.getScheduledAt() != null
            && (occurrenceState.getScheduledAt().isBefore(periodStart)
                || !occurrenceState.getScheduledAt().isBefore(periodEnd))) {
          continue;
        }
        taskResults.add(TaskResult.occurrence(series, occurrenceState, occurrenceScheduledAt));
      }

      for (TaskOccurrenceState movedState :
          movedInBySeries.getOrDefault(series.getId(), List.of())) {
        // State anchored outside the span its series still covers no longer overlays anything.
        if (!withinSeriesSpan(series, movedState.getOccurrenceScheduledAt())) {
          continue;
        }
        taskResults.add(
            TaskResult.occurrence(series, movedState, movedState.getOccurrenceScheduledAt()));
      }
    }

    return taskResults;
  }

  /**
   * Returns one recurring occurrence by its stable series and schedule identity.
   *
   * <p>Detached state is authoritative even when the current series no longer generates its anchor.
   * Attached or absent state must still identify an occurrence generated by the current recurrence
   * rule. Skipped occurrences are unavailable.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   * @return the addressed recurring occurrence
   */
  public TaskResult findOccurrence(UUID seriesId, Instant occurrenceScheduledAt) {
    requireOccurrenceIdentity(occurrenceScheduledAt, "to retrieve a recurring occurrence");
    Task series = taskService.findById(seriesId);
    if (!Boolean.TRUE.equals(series.getIsRecurring())) {
      throw new ResourceNotFoundException("Task is not recurring: " + seriesId);
    }

    Optional<TaskOccurrenceState> occurrenceState =
        taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            seriesId, occurrenceScheduledAt);
    if (occurrenceState.filter(TaskOccurrenceState::isDetached).isEmpty()) {
      validateOccurrence(series, occurrenceScheduledAt);
    }
    if (occurrenceState
        .map(TaskOccurrenceState::getStatus)
        .filter(TaskOccurrenceStatus.SKIPPED::equals)
        .isPresent()) {
      throw new ResourceNotFoundException(
          "Occurrence is skipped: " + occurrenceScheduledAt + " for task " + seriesId);
    }
    return TaskResult.occurrence(series, occurrenceState.orElse(null), occurrenceScheduledAt);
  }

  /**
   * Resolves the states a truncation strands: those anchored at or after the cut, which the series
   * no longer generates.
   *
   * <p>A completion and a deliberate override are kept and marked detached — they record something
   * the user did or placed, and stay displayable on their own date. A skip is an absence: once the
   * occurrence it excluded is gone, it has no object, so it is removed.
   *
   * @param seriesId recurring-series being truncated
   * @param cutInstant first instant the series no longer generates
   */
  @Transactional
  public void detachStatesFrom(UUID seriesId, Instant cutInstant) {
    List<TaskOccurrenceState> stranded =
        taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAtGreaterThanEqual(
            seriesId, cutInstant);
    for (TaskOccurrenceState occurrenceState : stranded) {
      if (occurrenceState.getStatus() == TaskOccurrenceStatus.SKIPPED) {
        taskOccurrenceStateRepository.delete(occurrenceState);
        continue;
      }
      occurrenceState.setDetached(true);
      taskOccurrenceStateRepository.save(occurrenceState);
    }
  }

  /** Returns whether the series has any persisted occurrence state. */
  public boolean hasPersistedStates(UUID seriesId) {
    return taskOccurrenceStateRepository.existsBySeriesId(seriesId);
  }

  /**
   * Tells whether an anchor still falls inside the stretch of time its series generates, that is
   * between its start and its truncation point.
   *
   * @param series recurring series the state belongs to
   * @param occurrenceScheduledAt anchor identifying the occurrence
   * @return {@code true} when the series still covers that instant
   */
  private boolean withinSeriesSpan(Task series, Instant occurrenceScheduledAt) {
    if (series.getScheduledAt() != null
        && occurrenceScheduledAt.isBefore(series.getScheduledAt())) {
      return false;
    }
    return series.getRruleEndsAt() == null
        || occurrenceScheduledAt.isBefore(series.getRruleEndsAt());
  }

  /**
   * Applies sparse overrides to one generated occurrence of a recurring series.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   * @param parameters occurrence fields to override
   * @param priorityProvided whether the caller explicitly supplied the priority field
   * @return the updated recurring occurrence
   */
  @Transactional
  public TaskResult updateOccurrence(
      UUID seriesId,
      Instant occurrenceScheduledAt,
      TaskPatchParameters parameters,
      boolean priorityProvided) {
    requireOccurrenceIdentity(occurrenceScheduledAt, "when scope is provided");
    Task series = taskService.findById(seriesId);
    TaskOccurrenceState occurrenceState =
        resolveMutableOccurrenceState(series, occurrenceScheduledAt)
            .orElseGet(TaskOccurrenceState::new);
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(TaskOccurrenceStatus.MODIFIED);
    if (parameters.content() != null) {
      occurrenceState.setTitle(parameters.content());
    }
    if (priorityProvided && parameters.priority() != null) {
      occurrenceState.setPriority(parameters.priority());
    }
    if (parameters.scheduledAt() != null) {
      Instant previousScheduledAt =
          occurrenceState.getScheduledAt() != null
              ? occurrenceState.getScheduledAt()
              : occurrenceScheduledAt;
      // Notification delivery is keyed by stable identity and resets only after rescheduling.
      if (!parameters.scheduledAt().equals(previousScheduledAt)) {
        taskOccurrenceNotificationService.clear(seriesId, occurrenceScheduledAt);
      }
      occurrenceState.setScheduledAt(parameters.scheduledAt());
      taskService.assertScheduleAllowed(
          series.getProjectId(), parameters.scheduledAt(), series.isAllDay());
    }
    if (parameters.dueAt() != null) {
      occurrenceState.setDueAt(parameters.dueAt());
    }
    return TaskResult.occurrence(
        series, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
  }

  /**
   * Replaces every supported override persisted for one recurring occurrence.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   * @param parameters replacement values for the occurrence overrides
   * @return the recurring occurrence with its replacement overrides
   */
  @Transactional
  public TaskResult replaceOccurrence(
      UUID seriesId, Instant occurrenceScheduledAt, TaskOccurrenceUpdateParameters parameters) {
    requireOccurrenceIdentity(occurrenceScheduledAt, "to replace a recurring occurrence");
    Task series = taskService.findById(seriesId);
    if (!Boolean.TRUE.equals(series.getIsRecurring())) {
      throw new IllegalArgumentException("Occurrence replacement requires a recurring task");
    }
    TaskOccurrenceState occurrenceState =
        resolveMutableOccurrenceState(series, occurrenceScheduledAt)
            .orElseGet(TaskOccurrenceState::new);
    Instant previousScheduledAt =
        occurrenceState.getScheduledAt() != null
            ? occurrenceState.getScheduledAt()
            : occurrenceScheduledAt;
    Instant replacementScheduledAt =
        parameters.scheduledAt() != null ? parameters.scheduledAt() : occurrenceScheduledAt;
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(TaskOccurrenceStatus.MODIFIED);
    occurrenceState.setTitle(parameters.title());
    occurrenceState.setPriority(parameters.priority());
    occurrenceState.setScheduledAt(parameters.scheduledAt());
    occurrenceState.setDueAt(parameters.dueAt());
    if (parameters.scheduledAt() != null) {
      taskService.assertScheduleAllowed(
          series.getProjectId(), parameters.scheduledAt(), series.isAllDay());
    }
    // Clearing by identity allows this occurrence to fire once at its replacement schedule.
    if (!replacementScheduledAt.equals(previousScheduledAt)) {
      taskOccurrenceNotificationService.clear(seriesId, occurrenceScheduledAt);
    }
    return TaskResult.occurrence(
        series, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
  }

  /**
   * Marks one generated occurrence as skipped without modifying its recurring series.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   */
  @Transactional
  public void skipOccurrence(UUID seriesId, Instant occurrenceScheduledAt) {
    requireOccurrenceIdentity(occurrenceScheduledAt, "to delete a recurring occurrence");
    Task series = taskService.findById(seriesId);
    TaskOccurrenceState occurrenceState =
        resolveMutableOccurrenceState(series, occurrenceScheduledAt)
            .orElseGet(TaskOccurrenceState::new);

    if (TaskOccurrenceStatus.SKIPPED.equals(occurrenceState.getStatus())) {
      throw new IllegalArgumentException(
          "Occurrence " + occurrenceState.getId() + " already skipped");
    }
    if (occurrenceState.getId() != null
        && occurrenceState.getStatus() == TaskOccurrenceStatus.DONE) {
      throw new IllegalStateException(
          "Cannot skip an already-completed occurrence ("
              + occurrenceState.getId()
              + "). Reopen it first.");
    }

    if (occurrenceState.isDetached()) {
      taskOccurrenceStateRepository.delete(occurrenceState);
      return;
    }

    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(TaskOccurrenceStatus.SKIPPED);
    taskOccurrenceStateRepository.save(occurrenceState);
  }

  /**
   * Marks one generated recurring occurrence as completed.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   * @return the completed recurring occurrence
   */
  @Transactional
  public TaskResult closeOccurrence(UUID seriesId, Instant occurrenceScheduledAt) {
    requireOccurrenceIdentity(occurrenceScheduledAt, "to complete a recurring occurrence");
    Task series = taskService.findById(seriesId);
    TaskOccurrenceState occurrenceState =
        resolveMutableOccurrenceState(series, occurrenceScheduledAt)
            .orElseGet(TaskOccurrenceState::new);
    if (occurrenceState.getId() != null
        && occurrenceState.getStatus() == TaskOccurrenceStatus.DONE) {
      throw new IllegalArgumentException("Occurrence already completed: " + occurrenceScheduledAt);
    }
    if (occurrenceState.getId() != null
        && occurrenceState.getStatus() == TaskOccurrenceStatus.SKIPPED) {
      throw new IllegalArgumentException("Cannot complete a skipped occurrence");
    }
    occurrenceState.setSeriesId(seriesId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(TaskOccurrenceStatus.DONE);
    occurrenceState.setCompletedAt(Instant.now());

    return TaskResult.occurrence(
        series, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
  }

  /**
   * Reopens one completed recurring occurrence while preserving any sparse overrides.
   *
   * @param seriesId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   * @return the reopened recurring occurrence
   */
  @Transactional
  public TaskResult reopenOccurrence(UUID seriesId, Instant occurrenceScheduledAt) {
    requireOccurrenceIdentity(occurrenceScheduledAt, "to reopen a recurring occurrence");
    Task series = taskService.findById(seriesId);
    TaskOccurrenceState occurrenceState =
        resolveMutableOccurrenceState(series, occurrenceScheduledAt)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Occurrence is not completed: " + occurrenceScheduledAt));
    if (occurrenceState.getStatus() != TaskOccurrenceStatus.DONE) {
      throw new IllegalArgumentException("Occurrence is not completed: " + occurrenceScheduledAt);
    }

    if (occurrenceState.isDetached() || hasOverrides(occurrenceState)) {
      // A completed modified occurrence returns to MODIFIED so its overrides remain observable.
      occurrenceState.setStatus(TaskOccurrenceStatus.MODIFIED);
      occurrenceState.setCompletedAt(null);
      return TaskResult.occurrence(
          series, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
    }

    // Absence of state is the canonical representation of an unmodified open occurrence.
    taskOccurrenceStateRepository.delete(occurrenceState);
    return TaskResult.occurrence(series, null, occurrenceScheduledAt);
  }

  /**
   * Verifies that the supplied instant is generated by the recurring series rule.
   *
   * @param series recurring series whose rule is evaluated
   * @param occurrenceScheduledAt candidate stable occurrence identity
   * @throws ResourceNotFoundException when the series does not generate this occurrence
   */
  public void validateOccurrence(Task series, Instant occurrenceScheduledAt) {
    Instant dayStart = occurrenceScheduledAt.truncatedTo(ChronoUnit.DAYS);
    Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);
    List<Instant> occurrences =
        taskRecurrenceService.getOccurrencesInRange(series, dayStart, dayEnd);
    if (!occurrences.contains(occurrenceScheduledAt)) {
      throw new ResourceNotFoundException(
          "No occurrence at " + occurrenceScheduledAt + " for task " + series.getId());
    }
  }

  /**
   * Resolves persisted state and accepts detached state by its stable stored identity. Attached or
   * absent state must still correspond to an occurrence generated by the current series rule.
   */
  private Optional<TaskOccurrenceState> resolveMutableOccurrenceState(
      Task series, Instant occurrenceScheduledAt) {
    Optional<TaskOccurrenceState> occurrenceState =
        taskOccurrenceStateRepository.findBySeriesIdAndOccurrenceScheduledAt(
            series.getId(), occurrenceScheduledAt);
    if (occurrenceState.filter(TaskOccurrenceState::isDetached).isEmpty()) {
      validateOccurrence(series, occurrenceScheduledAt);
    }
    return occurrenceState;
  }

  private static void requireOccurrenceIdentity(Instant occurrenceScheduledAt, String context) {
    if (occurrenceScheduledAt == null) {
      throw new IllegalArgumentException("occurrenceScheduledAt is required " + context);
    }
  }

  private static boolean hasOverrides(TaskOccurrenceState occurrenceState) {
    return occurrenceState.getTitle() != null
        || occurrenceState.getPriority() != null
        || occurrenceState.getScheduledAt() != null
        || occurrenceState.getDueAt() != null;
  }
}
