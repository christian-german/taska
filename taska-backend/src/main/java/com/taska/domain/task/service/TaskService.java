package com.taska.domain.task.service;

import com.taska.config.TaskaProperties;
import com.taska.domain.notification.service.TaskOccurrenceNotificationService;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.occurrence.repository.TaskOccurrenceStateRepository;
import com.taska.domain.task.occurrence.service.TaskRecurrenceService;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskOccurrenceStateRepository taskOccurrenceStateRepository;
  private final TaskRecurrenceService taskRecurrenceService;
  private final ProjectRepository projectRepository;
  private final TaskPriorityEvaluationRepository priorityEvaluationRepository;
  private final TaskaProperties taskaProperties;
  private final PlanningCalendarService planningCalendarService;
  private final TaskOccurrenceNotificationService taskOccurrenceNotificationService;

  /**
   * Returns a list of tasks scoped by the provided project or label criteria.
   *
   * @param projectId optional project ID to scope results
   * @param label optional label name to filter by
   * @param showCompleted when true, completed tasks are included in the result
   * @return list of matching tasks
   */
  @Transactional(readOnly = true)
  public List<Task> findAll(UUID projectId, String label, boolean showCompleted) {
    if (label != null) {
      return showCompleted
          ? taskRepository.findByLabel(label)
          : taskRepository.findByLabelAndIsCompletedFalse(label);
    }
    if (projectId != null) {
      return showCompleted
          ? taskRepository.findByProjectIdOrderByPositionAsc(projectId)
          : taskRepository.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
    }
    return taskRepository.findAll();
  }

  /**
   * Returns the task with the given ID, or throws {@link
   * com.taska.exception.ResourceNotFoundException} if no such task exists.
   *
   * @param taskId the task UUID
   * @return the matching task entity
   */
  @Transactional(readOnly = true)
  public Task findById(UUID taskId) {
    return getOrThrow(taskId);
  }

  /**
   * Creates and persists a new task from the given request. If no {@code projectId} is provided and
   * the task has no parent, it is placed in the inbox project. Defaults: position 0, not recurring,
   * and not all-day. Manual priority remains absent when the request does not provide one. The
   * recurrence rule is normalised from short aliases (e.g. "daily" → "FREQ=DAILY").
   *
   * @param taskCreateParameters the task creation payload
   * @return the persisted task entity
   */
  public Task create(TaskCreateParameters taskCreateParameters) {
    Task task = new Task();
    task.setContent(taskCreateParameters.content());
    task.setType(taskCreateParameters.type());
    task.setDescription(taskCreateParameters.description());
    task.setParentId(taskCreateParameters.parentId());
    task.setPosition(taskCreateParameters.position());
    task.setPriority(taskCreateParameters.priority());
    task.setLabels(
        taskCreateParameters.labels() != null ? taskCreateParameters.labels() : new ArrayList<>());
    task.setScheduledAt(taskCreateParameters.scheduledAt());
    task.setDueAt(taskCreateParameters.dueAt());
    task.setAllDay(taskCreateParameters.allDay());
    task.setIsRecurring(taskCreateParameters.recurring());
    task.setEstimateMinutes(taskCreateParameters.estimateMinutes());
    task.setMentionContext(taskCreateParameters.mentionContext());
    task.setRecurrenceRule(normalizeRRule(taskCreateParameters.recurrenceRule()));

    UUID projectId = taskCreateParameters.projectId();
    if (projectId == null && taskCreateParameters.parentId() == null) {
      projectId =
          projectRepository
              .findByIsInboxProjectTrue()
              .orElseThrow(() -> new ResourceNotFoundException("Inbox project not found"))
              .getId();
    }
    task.setProjectId(projectId);
    assertScheduleAllowed(projectId, task.getScheduledAt(), task.isAllDay());

    return taskRepository.save(task);
  }

  /**
   * Updates an existing task. The update strategy depends on whether the task is recurring and
   * whether a {@code scope} is provided:
   *
   * <ul>
   *   <li><b>No scope (or non-recurring task)</b> – a standard patch is applied directly to the
   *       task entity; only non-null request fields overwrite existing values.
   *   <li><b>{@code THIS_ONLY}</b> – creates or updates {@link TaskOccurrenceState} for the
   *       specified occurrence, overriding only {@code content}, {@code priority}, {@code
   *       scheduledAt}, and {@code dueAt}; all other occurrences remain unchanged.
   *   <li><b>{@code FROM_THIS}</b> – truncates the original series one second before {@code
   *       occurrenceScheduledAt}, then creates a new recurring task starting at that instant with
   *       the requested changes applied.
   * </ul>
   *
   * <p>{@code priorityProvided} retains JSON field-presence information: it distinguishes an
   * omitted priority from an explicit JSON null, the former leaving manual priority unchanged and
   * the latter clearing it.
   *
   * @param taskId the task UUID to update
   * @param taskPatchParameters the update payload; when {@code scope} is set, {@code
   *     occurrenceScheduledAt} must also be provided to identify the target occurrence
   * @param priorityProvided whether the caller explicitly supplied the priority field
   * @return the updated task (or occurrence) as a DTO
   * @throws IllegalArgumentException if {@code scope} is set but {@code occurrenceScheduledAt} is
   *     {@code null}
   * @throws ResourceNotFoundException if {@code THIS_ONLY} is requested but {@code
   *     occurrenceScheduledAt} does not match any occurrence generated by the task's RRULE
   */
  public TaskResult update(
      UUID taskId, TaskPatchParameters taskPatchParameters, boolean priorityProvided) {

    // Input validation.
    if (taskPatchParameters.scope() != null
        && taskPatchParameters.occurrenceScheduledAt() == null) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required when scope is provided");
    }

    Task task = getOrThrow(taskId);

    // Simple patch is applied for non-recurring tasks or when scope is null.
    if (taskPatchParameters.scope() == null || !Boolean.TRUE.equals(task.getIsRecurring())) {
      applyPatch(task, taskPatchParameters, priorityProvided);
      Task saved = taskRepository.save(task);
      priorityEvaluationRepository.deleteByTaskId(taskId);
      return TaskResult.base(saved);
    }

    // Recurring task update logic.
    Instant occurrenceScheduledAt = taskPatchParameters.occurrenceScheduledAt();
    return switch (taskPatchParameters.scope()) {
      case THIS_ONLY -> {
        validateOccurrence(task, occurrenceScheduledAt);
        TaskOccurrenceState occurrenceState =
            taskOccurrenceStateRepository
                .findBySeriesIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
                .orElseGet(TaskOccurrenceState::new);
        occurrenceState.setSeriesId(taskId);
        occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
        occurrenceState.setStatus(TaskOccurrenceStatus.MODIFIED);
        if (taskPatchParameters.content() != null) {
          occurrenceState.setTitle(taskPatchParameters.content());
        }
        if (priorityProvided && taskPatchParameters.priority() != null) {
          occurrenceState.setPriority(taskPatchParameters.priority());
        }
        if (taskPatchParameters.scheduledAt() != null) {
          Instant previousScheduledAt =
              occurrenceState.getScheduledAt() != null
                  ? occurrenceState.getScheduledAt()
                  : occurrenceScheduledAt;
          // Delivery state is keyed by the stable occurrence identity, but it is reset only when
          // the effective schedule actually changes.
          if (!taskPatchParameters.scheduledAt().equals(previousScheduledAt)) {
            taskOccurrenceNotificationService.clear(taskId, occurrenceScheduledAt);
          }
          occurrenceState.setScheduledAt(taskPatchParameters.scheduledAt());
        }
        if (taskPatchParameters.scheduledAt() != null) {
          assertScheduleAllowed(
              task.getProjectId(), taskPatchParameters.scheduledAt(), task.isAllDay());
        }
        if (taskPatchParameters.dueAt() != null) {
          occurrenceState.setDueAt(taskPatchParameters.dueAt());
        }
        yield TaskResult.occurrence(
            task, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
      }
      case FROM_THIS -> {
        task.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
        taskRepository.save(task);

        Task cloned = new Task();
        cloned.setContent(
            taskPatchParameters.content() != null
                ? taskPatchParameters.content()
                : task.getContent());
        cloned.setType(
            taskPatchParameters.type() != null ? taskPatchParameters.type() : task.getType());
        cloned.setDescription(
            taskPatchParameters.description() != null
                ? taskPatchParameters.description()
                : task.getDescription());
        cloned.setProjectId(task.getProjectId());
        cloned.setParentId(task.getParentId());
        cloned.setPosition(task.getPosition());
        cloned.setPriority(priorityProvided ? taskPatchParameters.priority() : task.getPriority());
        cloned.setLabels(
            taskPatchParameters.labels() != null ? taskPatchParameters.labels() : task.getLabels());
        cloned.setScheduledAt(occurrenceScheduledAt);
        cloned.setDueAt(
            taskPatchParameters.dueAt() != null ? taskPatchParameters.dueAt() : task.getDueAt());
        cloned.setAllDay(task.isAllDay());
        cloned.setIsRecurring(true);
        cloned.setEstimateMinutes(
            taskPatchParameters.estimateMinutes() != null
                ? taskPatchParameters.estimateMinutes()
                : task.getEstimateMinutes());
        cloned.setRecurrenceRule(
            taskPatchParameters.recurrenceRule() != null
                ? taskPatchParameters.recurrenceRule()
                : task.getRecurrenceRule());
        yield TaskResult.base(taskRepository.save(cloned));
      }
    };
  }

  /** Replaces every mutable field of a base task. */
  public TaskResult replace(UUID taskId, TaskUpdateParameters taskUpdateParameters) {
    Task task = getOrThrow(taskId);
    replaceMutableFields(task, taskUpdateParameters);
    Task saved = taskRepository.save(task);
    priorityEvaluationRepository.deleteByTaskId(taskId);
    return TaskResult.base(saved);
  }

  /** Splits a recurring series and creates its following replacement from the complete request. */
  public TaskResult replaceFollowing(
      UUID taskId, Instant occurrenceScheduledAt, TaskUpdateParameters taskUpdateParameters) {
    Task original = getOrThrow(taskId);
    if (!Boolean.TRUE.equals(original.getIsRecurring())) {
      throw new IllegalArgumentException("Following-series replacement requires a recurring task");
    }
    validateOccurrence(original, occurrenceScheduledAt);
    original.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
    taskRepository.save(original);

    Task replacement = new Task();
    replaceMutableFields(replacement, taskUpdateParameters);
    if (!Boolean.TRUE.equals(replacement.getIsRecurring())) {
      throw new IllegalArgumentException("Following-series replacement must remain recurring");
    }
    Task saved = taskRepository.save(replacement);
    priorityEvaluationRepository.deleteByTaskId(taskId);
    return TaskResult.base(saved);
  }

  /**
   * Returns all task occurrences (both regular and recurring) that fall within the given date
   * range. For recurring tasks, virtual occurrences are generated from the RRULE, with SKIPPED
   * occurrence states excluded and MODIFIED states merged in. Non-recurring tasks are included when
   * their due date falls inside the period.
   *
   * <p>Completed recurring occurrences are already represented by their persisted states, so {@code
   * showCompleted} only governs completed non-recurring tasks.
   *
   * @param from start of the date range (inclusive, UTC)
   * @param to end of the date range (inclusive, UTC)
   * @param showCompleted whether completed non-recurring tasks should be included
   * @return list of task DTOs, each representing a single occurrence
   */
  @Transactional(readOnly = true)
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

    List<Task> recurringSeries =
        taskRepository.findActiveRecurringTasksForPeriod(periodStart, periodEnd);
    if (recurringSeries.isEmpty()) {
      return taskResults;
    }

    List<UUID> recurringSeriesIds = recurringSeries.stream().map(Task::getId).toList();

    // State whose occurrenceScheduledAt falls within the period overlays matching RRULE
    // occurrences.
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

    // MODIFIED state whose scheduledAt was moved into this period from another day.
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
        // Skip occurrences whose scheduledAt was moved outside this period.
        if (occurrenceState != null
            && occurrenceState.getScheduledAt() != null
            && (occurrenceState.getScheduledAt().isBefore(periodStart)
                || !occurrenceState.getScheduledAt().isBefore(periodEnd))) {
          continue;
        }
        taskResults.add(TaskResult.occurrence(series, occurrenceState, occurrenceScheduledAt));
      }

      // Add occurrences that were rescheduled into this period from a different day.
      for (TaskOccurrenceState movedState :
          movedInBySeries.getOrDefault(series.getId(), List.of())) {
        taskResults.add(
            TaskResult.occurrence(series, movedState, movedState.getOccurrenceScheduledAt()));
      }
    }

    return taskResults;
  }

  /**
   * Replaces every supported override independently persisted for one recurring occurrence.
   *
   * @param taskId recurring series identifier
   * @param occurrenceScheduledAt stable RRULE-generated occurrence identity
   * @param taskOccurrenceUpdateParameters replacement values for the occurrence overrides
   * @return the recurring occurrence with its replacement overrides
   * @throws IllegalArgumentException if the task is not recurring
   * @throws ResourceNotFoundException if the task or occurrence does not exist
   */
  public TaskResult replaceOccurrence(
      UUID taskId,
      Instant occurrenceScheduledAt,
      TaskOccurrenceUpdateParameters taskOccurrenceUpdateParameters) {
    Task task = getOrThrow(taskId);
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("Occurrence replacement requires a recurring task");
    }
    validateOccurrence(task, occurrenceScheduledAt);
    TaskOccurrenceState occurrenceState =
        taskOccurrenceStateRepository
            .findBySeriesIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
            .orElseGet(TaskOccurrenceState::new);
    Instant previousScheduledAt =
        occurrenceState.getScheduledAt() != null
            ? occurrenceState.getScheduledAt()
            : occurrenceScheduledAt;
    Instant replacementScheduledAt =
        taskOccurrenceUpdateParameters.scheduledAt() != null
            ? taskOccurrenceUpdateParameters.scheduledAt()
            : occurrenceScheduledAt;
    occurrenceState.setSeriesId(taskId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(TaskOccurrenceStatus.MODIFIED);
    occurrenceState.setTitle(taskOccurrenceUpdateParameters.title());
    occurrenceState.setPriority(taskOccurrenceUpdateParameters.priority());
    occurrenceState.setScheduledAt(taskOccurrenceUpdateParameters.scheduledAt());
    occurrenceState.setDueAt(taskOccurrenceUpdateParameters.dueAt());
    if (taskOccurrenceUpdateParameters.scheduledAt() != null) {
      assertScheduleAllowed(
          task.getProjectId(), taskOccurrenceUpdateParameters.scheduledAt(), task.isAllDay());
    }
    // Clearing by identity allows the same occurrence to fire once at its replacement schedule.
    if (!replacementScheduledAt.equals(previousScheduledAt)) {
      taskOccurrenceNotificationService.clear(taskId, occurrenceScheduledAt);
    }
    return TaskResult.occurrence(
        task, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
  }

  /**
   * Deletes a task. The deletion strategy depends on the provided scope:
   *
   * <ul>
   *   <li><b>No scope (or non-recurring task)</b> – the task entity is permanently deleted.
   *   <li><b>{@code THIS_ONLY}</b> – marks the specified occurrence as SKIPPED by creating {@link
   *       TaskOccurrenceState}; the rest of the series remains intact.
   *   <li><b>{@code FROM_THIS}</b> – truncates the series one second before {@code
   *       occurrenceScheduledAt} so that no occurrences are generated from that point onwards.
   * </ul>
   *
   * @param taskId the task UUID to delete
   * @param taskDeleteParameters optional delete request containing the scope and the scheduled
   *     occurrence instant; when {@code null} or when scope is {@code null}, the task is
   *     permanently deleted regardless of whether it is recurring
   * @throws ResourceNotFoundException if {@code THIS_ONLY} is requested but {@code
   *     occurrenceScheduledAt} does not match any occurrence generated by the task's RRULE
   * @throws IllegalArgumentException if {@code THIS_ONLY} is requested but the occurrence is
   *     already skipped
   * @throws IllegalStateException if {@code THIS_ONLY} is requested but the occurrence is already
   *     completed (reopen it first before skipping)
   */
  public void delete(UUID taskId, TaskDeleteParameters taskDeleteParameters) {
    Task task = getOrThrow(taskId);

    if (taskDeleteParameters == null
        || taskDeleteParameters.scope() == null
        || !Boolean.TRUE.equals(task.getIsRecurring())) {
      priorityEvaluationRepository.deleteByTaskId(taskId);
      taskRepository.delete(task);
      return;
    }

    switch (taskDeleteParameters.scope()) {
      case THIS_ONLY -> {
        validateOccurrence(task, taskDeleteParameters.occurrenceScheduledAt());
        TaskOccurrenceState occurrenceState =
            taskOccurrenceStateRepository
                .findBySeriesIdAndOccurrenceScheduledAt(
                    taskId, taskDeleteParameters.occurrenceScheduledAt())
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

        occurrenceState.setSeriesId(taskId);
        occurrenceState.setOccurrenceScheduledAt(taskDeleteParameters.occurrenceScheduledAt());
        occurrenceState.setStatus(TaskOccurrenceStatus.SKIPPED);
        taskOccurrenceStateRepository.save(occurrenceState);
      }
      case FROM_THIS -> {
        task.setRruleEndsAt(
            taskDeleteParameters.occurrenceScheduledAt().minus(1, ChronoUnit.SECONDS));
        taskRepository.save(task);
      }
    }
  }

  /**
   * Marks a task as completed. For non-recurring tasks the task entity is flagged as completed with
   * the current timestamp. For recurring tasks, {@link TaskOccurrenceState} with status DONE is
   * created for the given occurrence.
   *
   * @param taskId the task UUID
   * @param taskCloseReopenParameters request containing the scheduled occurrence instant; required
   *     for recurring tasks
   * @return the updated non-recurring task or completed recurring occurrence
   * @throws IllegalArgumentException if the task is recurring and {@code occurrenceScheduledAt} is
   *     not provided
   * @throws ResourceNotFoundException if {@code occurrenceScheduledAt} does not match any
   *     occurrence generated by the task's RRULE
   * @throws IllegalArgumentException if the specified recurring occurrence is already completed or
   *     skipped
   */
  public TaskResult close(UUID taskId, TaskCloseReopenParameters taskCloseReopenParameters) {
    Task task = getOrThrow(taskId);
    Instant occurrenceScheduledAt =
        taskCloseReopenParameters != null
            ? taskCloseReopenParameters.occurrenceScheduledAt()
            : null;

    // Validate input.
    if (Boolean.TRUE.equals(task.getIsRecurring()) && occurrenceScheduledAt == null) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required to complete a recurring occurrence");
    }

    // Non-recurring tasks are completed by setting the completion flag and timestamp.
    if (!Boolean.TRUE.equals(task.getIsRecurring()) || occurrenceScheduledAt == null) {
      task.setIsCompleted(true);
      task.setCompletedAt(Instant.now());
      Task saved = taskRepository.save(task);
      priorityEvaluationRepository.deleteByTaskId(taskId);
      return TaskResult.base(saved);
    }

    // For recurring tasks, occurrence state is created or updated with status DONE.
    validateOccurrence(task, occurrenceScheduledAt);
    TaskOccurrenceState occurrenceState =
        taskOccurrenceStateRepository
            .findBySeriesIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
            .orElseGet(TaskOccurrenceState::new);
    if (occurrenceState.getId() != null
        && occurrenceState.getStatus() == TaskOccurrenceStatus.DONE) {
      throw new IllegalArgumentException("Occurrence already completed: " + occurrenceScheduledAt);
    }
    if (occurrenceState.getId() != null
        && occurrenceState.getStatus() == TaskOccurrenceStatus.SKIPPED) {
      throw new IllegalArgumentException("Cannot complete a skipped occurrence");
    }
    occurrenceState.setSeriesId(taskId);
    occurrenceState.setOccurrenceScheduledAt(occurrenceScheduledAt);
    occurrenceState.setStatus(TaskOccurrenceStatus.DONE);
    occurrenceState.setCompletedAt(Instant.now());

    return TaskResult.occurrence(
        task, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
  }

  /**
   * Reopens a previously completed task. For non-recurring tasks the completion flag and timestamp
   * are cleared. For recurring tasks, the {@link TaskOccurrenceState} representing the completed
   * occurrence is restored to its previous modified state, or deleted when it has no overrides.
   *
   * @param taskId the task UUID
   * @param taskCloseReopenParameters optional request containing the scheduled occurrence instant
   *     for recurring tasks
   * @return the reopened non-recurring task or recurring occurrence
   * @throws IllegalArgumentException if a recurring occurrence identity is missing or the
   *     occurrence is not completed
   * @throws ResourceNotFoundException if the task or recurring occurrence does not exist
   */
  public TaskResult reopen(UUID taskId, TaskCloseReopenParameters taskCloseReopenParameters) {
    Task task = getOrThrow(taskId);
    Instant occurrenceScheduledAt =
        taskCloseReopenParameters != null
            ? taskCloseReopenParameters.occurrenceScheduledAt()
            : null;

    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      task.setIsCompleted(false);
      task.setCompletedAt(null);
      return TaskResult.base(taskRepository.save(task));
    }

    if (occurrenceScheduledAt == null) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required to reopen a recurring occurrence");
    }
    validateOccurrence(task, occurrenceScheduledAt);
    TaskOccurrenceState occurrenceState =
        taskOccurrenceStateRepository
            .findBySeriesIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Occurrence is not completed: " + occurrenceScheduledAt));
    if (occurrenceState.getStatus() != TaskOccurrenceStatus.DONE) {
      throw new IllegalArgumentException("Occurrence is not completed: " + occurrenceScheduledAt);
    }

    if (hasOverrides(occurrenceState)) {
      // DONE is also used for previously modified occurrences; restore that prior open state.
      occurrenceState.setStatus(TaskOccurrenceStatus.MODIFIED);
      occurrenceState.setCompletedAt(null);
      return TaskResult.occurrence(
          task, taskOccurrenceStateRepository.save(occurrenceState), occurrenceScheduledAt);
    }

    // With no sparse overrides left to preserve, absence is the canonical open representation.
    taskOccurrenceStateRepository.delete(occurrenceState);
    return TaskResult.occurrence(task, null, occurrenceScheduledAt);
  }

  /**
   * Returns all direct subtasks of the given parent task, ordered by their position.
   *
   * @param parentTaskId the UUID of the parent task
   * @return list of subtask entities
   */
  @Transactional(readOnly = true)
  public List<Task> getSubtasks(UUID parentTaskId) {
    return taskRepository.findByParentIdOrderByPositionAsc(parentTaskId);
  }

  /**
   * Loads a task by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not
   * found.
   *
   * @param taskId the task UUID
   * @return the task entity
   */
  private Task getOrThrow(UUID taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
  }

  /**
   * Returns all incomplete, non-notified, non-all-day tasks whose due date is at or before the
   * given instant. Used by the notification scheduler to find tasks due in approximately 15
   * minutes.
   *
   * @param instant the upper-bound instant (typically now + 15 minutes)
   * @return list of tasks eligible for a push notification
   */
  public List<Task> findTasksDueAround(Instant instant) {
    return taskRepository.findTasksDueAround(instant);
  }

  /**
   * Validates that {@code occurrenceScheduledAt} corresponds to a real occurrence generated by the
   * task's RRULE. The check is performed by expanding the RRULE over the full day that contains
   * {@code occurrenceScheduledAt} and verifying that the exact instant is present.
   *
   * @param task the recurring task whose RRULE is checked
   * @param occurrenceScheduledAt the candidate occurrence instant
   * @throws ResourceNotFoundException if {@code occurrenceScheduledAt} does not match any computed
   *     occurrence
   */
  private void validateOccurrence(Task task, Instant occurrenceScheduledAt) {
    Instant dayStart = occurrenceScheduledAt.truncatedTo(ChronoUnit.DAYS);
    Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);
    List<Instant> occurrences = taskRecurrenceService.getOccurrencesInRange(task, dayStart, dayEnd);
    if (!occurrences.contains(occurrenceScheduledAt)) {
      throw new ResourceNotFoundException(
          "No occurrence at " + occurrenceScheduledAt + " for task " + task.getId());
    }
  }

  /**
   * Converts shorthand recurrence aliases to valid iCal4j RRULE strings. If {@code
   * recurrenceStringRule} is already a valid RRULE (starts with "FREQ="), it is returned unchanged.
   * Null and blank inputs are passed through as-is.
   *
   * @param recurrenceStringRule raw recurrence rule string (e.g. "daily", "FREQ=WEEKLY")
   * @return normalised RRULE string or the original {@code recurrenceStringRule}
   */
  private static String normalizeRRule(String recurrenceStringRule) {
    if (recurrenceStringRule == null
        || recurrenceStringRule.isBlank()
        || recurrenceStringRule.toUpperCase().startsWith("FREQ=")) {
      return recurrenceStringRule;
    }
    return switch (recurrenceStringRule.toLowerCase()) {
      case "daily" -> "FREQ=DAILY";
      case "weekly" -> "FREQ=WEEKLY";
      case "monthly" -> "FREQ=MONTHLY";
      case "yearly" -> "FREQ=YEARLY";
      default -> recurrenceStringRule;
    };
  }

  /**
   * Applies non-null fields from the request onto an existing task entity. When the planned
   * scheduled time changes, the notification flag is reset so the task can trigger a new push
   * notification at its new time.
   *
   * @param task the task entity to mutate in-place
   * @param taskPatchParameters the update payload; only non-null fields are applied
   * @param priorityProvided whether the caller explicitly supplied the priority field
   */
  private void applyPatch(
      Task task, TaskPatchParameters taskPatchParameters, boolean priorityProvided) {
    if (taskPatchParameters.content() != null) {
      task.setContent(taskPatchParameters.content());
    }
    if (taskPatchParameters.type() != null) {
      task.setType(taskPatchParameters.type());
    }
    if (taskPatchParameters.description() != null) {
      task.setDescription(taskPatchParameters.description());
    }
    if (taskPatchParameters.projectId() != null) {
      task.setProjectId(taskPatchParameters.projectId());
    }
    if (taskPatchParameters.parentId() != null) {
      task.setParentId(taskPatchParameters.parentId());
    }
    if (taskPatchParameters.position() != null) {
      task.setPosition(taskPatchParameters.position());
    }
    if (priorityProvided) {
      task.setPriority(taskPatchParameters.priority());
    }
    if (taskPatchParameters.labels() != null) {
      task.setLabels(taskPatchParameters.labels());
    }
    if (taskPatchParameters.allDay() != null) {
      task.setAllDay(taskPatchParameters.allDay());
    }
    if (taskPatchParameters.recurring() != null) {
      task.setIsRecurring(taskPatchParameters.recurring());
    }
    if (taskPatchParameters.estimateMinutes() != null) {
      task.setEstimateMinutes(taskPatchParameters.estimateMinutes());
    }
    if (taskPatchParameters.mentionContext() != null) {
      task.setMentionContext(taskPatchParameters.mentionContext());
    }
    if (taskPatchParameters.recurrenceRule() != null) {
      task.setRecurrenceRule(normalizeRRule(taskPatchParameters.recurrenceRule()));
    }
    if (taskPatchParameters.dueAt() != null) {
      task.setDueAt(taskPatchParameters.dueAt());
    }
    if (taskPatchParameters.scheduledAt() != null) {
      UUID effectiveProjectId =
          taskPatchParameters.projectId() != null
              ? taskPatchParameters.projectId()
              : task.getProjectId();
      boolean effectiveAllDay =
          taskPatchParameters.allDay() != null ? taskPatchParameters.allDay() : task.isAllDay();
      assertScheduleAllowed(effectiveProjectId, taskPatchParameters.scheduledAt(), effectiveAllDay);
      if (!taskPatchParameters.scheduledAt().equals(task.getScheduledAt())) {
        task.setIsNotified(false);
      }
      task.setScheduledAt(taskPatchParameters.scheduledAt());
    }
  }

  private void replaceMutableFields(Task task, TaskUpdateParameters taskUpdateParameters) {
    task.setContent(taskUpdateParameters.content());
    task.setType(taskUpdateParameters.type());
    task.setDescription(taskUpdateParameters.description());
    task.setProjectId(taskUpdateParameters.projectId());
    task.setParentId(taskUpdateParameters.parentId());
    task.setPosition(taskUpdateParameters.position());
    task.setPriority(taskUpdateParameters.priority());
    task.setLabels(new ArrayList<>(taskUpdateParameters.labels()));
    if (!java.util.Objects.equals(task.getScheduledAt(), taskUpdateParameters.scheduledAt())) {
      task.setIsNotified(false);
    }
    task.setScheduledAt(taskUpdateParameters.scheduledAt());
    task.setDueAt(taskUpdateParameters.dueAt());
    task.setAllDay(taskUpdateParameters.allDay());
    task.setIsRecurring(taskUpdateParameters.recurring());
    task.setEstimateMinutes(taskUpdateParameters.estimateMinutes());
    task.setMentionContext(taskUpdateParameters.mentionContext());
    task.setRecurrenceRule(normalizeRRule(taskUpdateParameters.recurrenceRule()));
    assertScheduleAllowed(
        taskUpdateParameters.projectId(),
        taskUpdateParameters.scheduledAt(),
        taskUpdateParameters.allDay());
  }

  private void assertScheduleAllowed(UUID projectId, Instant scheduledAt, boolean allDay) {
    if (scheduledAt == null || projectId == null) {
      return;
    }
    UUID calendarId =
        projectRepository
            .findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId))
            .getPlanningCalendarId();
    if (!planningCalendarService.allows(calendarId, scheduledAt, allDay)) {
      throw new IllegalArgumentException(
          "Scheduled time is outside the project's planning calendar availability");
    }
  }

  private boolean hasOverrides(TaskOccurrenceState occurrenceState) {
    return occurrenceState.getTitle() != null
        || occurrenceState.getPriority() != null
        || occurrenceState.getScheduledAt() != null
        || occurrenceState.getDueAt() != null;
  }
}
