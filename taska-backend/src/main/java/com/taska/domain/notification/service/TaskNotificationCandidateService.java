package com.taska.domain.notification.service;

import com.taska.domain.task.occurrence.TaskOccurrenceState;
import com.taska.domain.task.occurrence.TaskOccurrenceStatus;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.domain.task.service.RecurringTaskOccurrenceResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Finds non-recurring tasks and recurring occurrences eligible for scheduled notification. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskNotificationCandidateService {

  private final TaskRepository taskRepository;
  private final TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  private final TaskRecurrenceService taskRecurrenceService;

  /**
   * Finds notification targets for one scheduler run.
   *
   * <p>Non-recurring tasks retain their historical cutoff-based eligibility. Recurring occurrences
   * must have an effective schedule within the bounded upcoming window.
   *
   * @param windowStart inclusive start of the recurring-occurrence window
   * @param windowEnd inclusive end of the recurring-occurrence window and legacy task cutoff
   * @return eligible task-like candidates using resolved occurrence values
   */
  public List<TaskNotificationCandidate> findEligible(Instant windowStart, Instant windowEnd) {
    List<TaskNotificationCandidate> candidates =
        new ArrayList<>(
            taskRepository.findTasksDueAround(windowEnd).stream()
                .map(TaskNotificationCandidate::nonRecurring)
                .toList());

    Map<UUID, Task> recurringSeriesById =
        taskRepository.findActiveRecurringTasksForPeriod(windowStart, windowEnd).stream()
            .filter(this::eligibleRecurringTask)
            .collect(
                Collectors.toMap(
                    Task::getId,
                    task -> task,
                    (existingTask, _) -> existingTask,
                    LinkedHashMap::new));

    // Discover moved occurrences independently: their series may not otherwise overlap the
    // current window, for example, when a future occurrence was moved earlier.
    List<TaskOccurrenceState> modifiedStatesInWindow =
        taskOccurrenceStateRepository.findByStatusAndScheduledAtBetween(
            TaskOccurrenceStatus.MODIFIED, windowStart, windowEnd);
    Set<UUID> missingSeriesIds =
        modifiedStatesInWindow.stream()
            .map(TaskOccurrenceState::getSeriesId)
            .filter(seriesId -> !recurringSeriesById.containsKey(seriesId))
            .collect(Collectors.toSet());
    taskRepository.findAllById(missingSeriesIds).stream()
        .filter(this::eligibleRecurringTask)
        .forEach(series -> recurringSeriesById.put(series.getId(), series));

    if (recurringSeriesById.isEmpty()) {
      return candidates;
    }

    List<UUID> recurringSeriesIds = List.copyOf(recurringSeriesById.keySet());

    // State in the identity window overlays virtual RRULE occurrences. Moved-in state is indexed
    // separately because its stable identity lies outside that window.
    Map<UUID, Map<Instant, TaskOccurrenceState>> statesBySeries =
        taskOccurrenceStateRepository
            .findBySeriesIdInAndOccurrenceScheduledAtBetween(
                recurringSeriesIds, windowStart, windowEnd)
            .stream()
            .collect(
                Collectors.groupingBy(
                    TaskOccurrenceState::getSeriesId,
                    Collectors.toMap(
                        TaskOccurrenceState::getOccurrenceScheduledAt,
                        occurrenceState -> occurrenceState,
                        (existingState, _) -> existingState)));
    Map<UUID, List<TaskOccurrenceState>> movedInBySeries =
        modifiedStatesInWindow.stream()
            .filter(
                occurrenceState -> recurringSeriesById.containsKey(occurrenceState.getSeriesId()))
            .filter(
                occurrenceState ->
                    outsideWindow(
                        occurrenceState.getOccurrenceScheduledAt(), windowStart, windowEnd))
            .collect(Collectors.groupingBy(TaskOccurrenceState::getSeriesId));

    for (Task recurringSeries : recurringSeriesById.values()) {
      Map<Instant, TaskOccurrenceState> occurrenceStates =
          statesBySeries.getOrDefault(recurringSeries.getId(), Map.of());
      for (Instant occurrenceScheduledAt :
          taskRecurrenceService.getOccurrencesInRange(recurringSeries, windowStart, windowEnd)) {
        TaskOccurrenceState occurrenceState = occurrenceStates.get(occurrenceScheduledAt);
        if (unavailable(occurrenceState)) {
          continue;
        }
        RecurringTaskOccurrenceResult occurrence =
            new RecurringTaskOccurrenceResult(
                recurringSeries, occurrenceState, occurrenceScheduledAt);

        // A schedule override can move an identity-window occurrence outside the effective window.
        if (withinWindow(occurrence.resolvedScheduledAt(), windowStart, windowEnd)) {
          candidates.add(TaskNotificationCandidate.recurring(occurrence));
        }
      }

      for (TaskOccurrenceState movedState :
          movedInBySeries.getOrDefault(recurringSeries.getId(), List.of())) {
        RecurringTaskOccurrenceResult occurrence =
            new RecurringTaskOccurrenceResult(
                recurringSeries, movedState, movedState.getOccurrenceScheduledAt());
        candidates.add(TaskNotificationCandidate.recurring(occurrence));
      }
    }

    return candidates;
  }

  private boolean unavailable(TaskOccurrenceState occurrenceState) {
    return occurrenceState != null
        && (occurrenceState.getStatus() == TaskOccurrenceStatus.DONE
            || occurrenceState.getStatus() == TaskOccurrenceStatus.SKIPPED);
  }

  private boolean eligibleRecurringTask(Task task) {
    return Boolean.TRUE.equals(task.getIsRecurring()) && !task.isAllDay();
  }

  private boolean outsideWindow(Instant instant, Instant windowStart, Instant windowEnd) {
    return !withinWindow(instant, windowStart, windowEnd);
  }

  private boolean withinWindow(Instant instant, Instant windowStart, Instant windowEnd) {
    return !instant.isBefore(windowStart) && !instant.isAfter(windowEnd);
  }
}
