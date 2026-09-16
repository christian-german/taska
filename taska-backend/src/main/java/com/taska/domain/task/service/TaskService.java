package com.taska.domain.task.service;

import com.taska.config.TaskaProperties;
import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.occurrence.*;
import com.taska.domain.task.occurrence.repository.TaskInstanceRepository;
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
  private final TaskInstanceRepository taskInstanceRepository;
  private final TaskRecurrenceService taskRecurrenceService;
  private final ProjectRepository projectRepository;
  private final TaskPriorityEvaluationRepository priorityEvaluationRepository;
  private final TaskaProperties taskaProperties;
  private final PlanningCalendarService planningCalendarService;

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
   *   <li><b>{@code THIS_ONLY}</b> – creates or updates a {@link TaskInstance} for the specified
   *       occurrence, overriding only {@code content}, {@code priority}, {@code scheduledAt}, and
   *       {@code dueAt}; all other occurrences remain unchanged.
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
        TaskInstance instance =
            taskInstanceRepository
                .findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
                .orElseGet(TaskInstance::new);
        instance.setTaskId(taskId);
        instance.setOccurrenceScheduledAt(occurrenceScheduledAt);
        instance.setStatus(TaskInstanceStatus.MODIFIED);
        if (taskPatchParameters.content() != null) {
          instance.setTitle(taskPatchParameters.content());
        }
        if (priorityProvided && taskPatchParameters.priority() != null) {
          instance.setPriority(taskPatchParameters.priority());
        }
        if (taskPatchParameters.scheduledAt() != null) {
          instance.setScheduledAt(taskPatchParameters.scheduledAt());
        }
        if (taskPatchParameters.scheduledAt() != null) {
          assertScheduleAllowed(
              task.getProjectId(), taskPatchParameters.scheduledAt(), task.isAllDay());
        }
        if (taskPatchParameters.dueAt() != null) {
          instance.setDueAt(taskPatchParameters.dueAt());
        }
        yield TaskResult.occurrence(
            task, taskInstanceRepository.save(instance), occurrenceScheduledAt);
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
   * instances excluded and MODIFIED instances merged in. Non-recurring tasks are included when
   * their due date falls inside the period.
   *
   * <p>Completed recurring occurrences are already represented by their task instances, so {@code
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

    List<Task> recurringTasks =
        taskRepository.findActiveRecurringTasksForPeriod(periodStart, periodEnd);
    if (recurringTasks.isEmpty()) {
      return taskResults;
    }

    List<UUID> recurringTaskIds = recurringTasks.stream().map(Task::getId).toList();

    // Instances whose occurrenceScheduledAt falls within the period (for RRULE occurrence
    // matching).
    Map<UUID, Map<Instant, TaskInstance>> instancesByTask =
        taskInstanceRepository
            .findByTaskIdInAndOccurrenceScheduledAtBetween(recurringTaskIds, periodStart, periodEnd)
            .stream()
            .collect(
                Collectors.groupingBy(
                    TaskInstance::getTaskId,
                    Collectors.toMap(
                        TaskInstance::getOccurrenceScheduledAt,
                        taskInstance -> taskInstance,
                        (existingInstance, _) -> existingInstance)));

    // MODIFIED instances whose scheduledAt was moved into this period from another day.
    Map<UUID, List<TaskInstance>> movedInByTask =
        taskInstanceRepository
            .findByTaskIdInAndStatusAndScheduledAtBetween(
                recurringTaskIds, TaskInstanceStatus.MODIFIED, periodStart, periodEnd)
            .stream()
            .filter(
                taskInstance ->
                    taskInstance.getOccurrenceScheduledAt().isBefore(periodStart)
                        || !taskInstance.getOccurrenceScheduledAt().isBefore(periodEnd))
            .collect(Collectors.groupingBy(TaskInstance::getTaskId));

    for (Task task : recurringTasks) {
      Map<Instant, TaskInstance> taskInstances =
          instancesByTask.getOrDefault(task.getId(), Map.of());
      List<Instant> occurrences =
          taskRecurrenceService.getOccurrencesInRange(task, periodStart, periodEnd);

      for (Instant occurrenceScheduledAt : occurrences) {
        TaskInstance instance = taskInstances.get(occurrenceScheduledAt);
        if (instance != null && instance.getStatus() == TaskInstanceStatus.SKIPPED) {
          continue;
        }
        // Skip occurrences whose scheduledAt was moved outside this period.
        if (instance != null
            && instance.getScheduledAt() != null
            && (instance.getScheduledAt().isBefore(periodStart)
                || !instance.getScheduledAt().isBefore(periodEnd))) {
          continue;
        }
        taskResults.add(TaskResult.occurrence(task, instance, occurrenceScheduledAt));
      }

      // Add occurrences that were rescheduled into this period from a different day.
      for (TaskInstance movedInstance : movedInByTask.getOrDefault(task.getId(), List.of())) {
        taskResults.add(
            TaskResult.occurrence(task, movedInstance, movedInstance.getOccurrenceScheduledAt()));
      }
    }

    return taskResults;
  }

  /** Replaces the fields independently persisted for one recurring occurrence. */
  public TaskResult replaceOccurrence(
      UUID taskId,
      Instant occurrenceScheduledAt,
      TaskOccurrenceUpdateParameters taskOccurrenceUpdateParameters) {
    Task task = getOrThrow(taskId);
    if (!Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException("Occurrence replacement requires a recurring task");
    }
    validateOccurrence(task, occurrenceScheduledAt);
    TaskInstance instance =
        taskInstanceRepository
            .findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
            .orElseGet(TaskInstance::new);
    instance.setTaskId(taskId);
    instance.setOccurrenceScheduledAt(occurrenceScheduledAt);
    instance.setStatus(TaskInstanceStatus.MODIFIED);
    instance.setTitle(taskOccurrenceUpdateParameters.title());
    instance.setPriority(taskOccurrenceUpdateParameters.priority());
    instance.setScheduledAt(taskOccurrenceUpdateParameters.scheduledAt());
    instance.setDueAt(taskOccurrenceUpdateParameters.dueAt());
    if (taskOccurrenceUpdateParameters.scheduledAt() != null) {
      assertScheduleAllowed(
          task.getProjectId(), taskOccurrenceUpdateParameters.scheduledAt(), task.isAllDay());
    }
    return TaskResult.occurrence(
        task, taskInstanceRepository.save(instance), occurrenceScheduledAt);
  }

  /**
   * Deletes a task. The deletion strategy depends on the provided scope:
   *
   * <ul>
   *   <li><b>No scope (or non-recurring task)</b> – the task entity is permanently deleted.
   *   <li><b>{@code THIS_ONLY}</b> – marks the specified occurrence as SKIPPED by creating a {@link
   *       TaskInstance}; the rest of the series remains intact.
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
        TaskInstance taskInstance =
            taskInstanceRepository
                .findByTaskIdAndOccurrenceScheduledAt(
                    taskId, taskDeleteParameters.occurrenceScheduledAt())
                .orElseGet(TaskInstance::new);

        if (TaskInstanceStatus.SKIPPED.equals(taskInstance.getStatus())) {
          throw new IllegalArgumentException(
              "Occurrence " + taskInstance.getId() + " already skipped");
        }

        if (taskInstance.getId() != null && taskInstance.getStatus() == TaskInstanceStatus.DONE) {
          throw new IllegalStateException(
              "Cannot skip an already-completed occurrence ("
                  + taskInstance.getId()
                  + "). Reopen it first.");
        }

        taskInstance.setTaskId(taskId);
        taskInstance.setOccurrenceScheduledAt(taskDeleteParameters.occurrenceScheduledAt());
        taskInstance.setStatus(TaskInstanceStatus.SKIPPED);
        taskInstanceRepository.save(taskInstance);
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
   * the current timestamp. For recurring tasks, a {@link TaskInstance} with status DONE is created
   * for the given occurrence.
   *
   * @param taskId the task UUID
   * @param taskCloseReopenParameters request containing the scheduled occurrence instant; required
   *     for recurring tasks
   * @return the updated task as a DTO
   * @throws IllegalArgumentException if the task is recurring and {@code occurrenceScheduledAt} is
   *     not provided
   * @throws ResourceNotFoundException if {@code occurrenceScheduledAt} does not match any
   *     occurrence generated by the task's RRULE
   * @throws IllegalArgumentException if the specified recurring occurrence is already completed
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

    // For recurring tasks, a TaskInstance is created or updated with status DONE for the given
    // occurrence.
    validateOccurrence(task, occurrenceScheduledAt);
    TaskInstance instance =
        taskInstanceRepository
            .findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
            .orElseGet(TaskInstance::new);
    if (instance.getId() != null && instance.getStatus() == TaskInstanceStatus.DONE) {
      throw new IllegalArgumentException("Occurrence already completed: " + occurrenceScheduledAt);
    }
    instance.setTaskId(taskId);
    instance.setOccurrenceScheduledAt(occurrenceScheduledAt);
    instance.setStatus(TaskInstanceStatus.DONE);
    instance.setCompletedAt(Instant.now());

    return TaskResult.occurrence(
        task, taskInstanceRepository.save(instance), occurrenceScheduledAt);
  }

  /**
   * Reopens a previously completed task. For non-recurring tasks the completion flag and timestamp
   * are cleared. For recurring tasks, the {@link TaskInstance} representing the completed
   * occurrence is deleted, restoring it to its virtual (open) state.
   *
   * @param taskId the task UUID
   * @param taskCloseReopenParameters optional request containing the scheduled occurrence instant
   *     for recurring tasks
   * @return the updated task as a DTO
   */
  public TaskResult reopen(UUID taskId, TaskCloseReopenParameters taskCloseReopenParameters) {
    Task task = getOrThrow(taskId);
    Instant occurrenceScheduledAt =
        taskCloseReopenParameters != null
            ? taskCloseReopenParameters.occurrenceScheduledAt()
            : null;

    if (!Boolean.TRUE.equals(task.getIsRecurring()) || occurrenceScheduledAt == null) {
      task.setIsCompleted(false);
      task.setCompletedAt(null);
      return TaskResult.base(taskRepository.save(task));
    }

    taskInstanceRepository.deleteByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt);
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
}
