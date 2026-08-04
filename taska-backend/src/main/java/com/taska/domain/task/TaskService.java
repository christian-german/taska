package com.taska.domain.task;

import com.taska.config.TaskaProperties;
import com.taska.domain.project.ProjectRepository;
import com.taska.domain.priority.TaskPriorityEvaluationRepository;
import com.taska.exception.ResourceNotFoundException;
import com.taska.domain.planningcalendar.PlanningCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final RecurrenceService recurrenceService;
    private final TaskMapper taskMapper;
    private final ProjectRepository projectRepository;
    private final TaskPriorityEvaluationRepository priorityEvaluationRepository;
    private final TaskaProperties taskaProperties;
    private final PlanningCalendarService planningCalendarService;

    /**
     * Returns a filtered list of tasks based on the provided criteria.
     * Supported filters: "today" (tasks due today), "overdue" (past-due non-recurring),
     * "upcoming" (due in the next 14 days). Label and project/section filters are
     * applied when the corresponding parameter is non-null.
     *
     * @param projectId     optional project ID to scope results
     * @param sectionId     optional section ID to scope results
     * @param label         optional label name to filter by
     * @param filter        optional named filter: "today", "overdue", or "upcoming"
     * @param showCompleted when true, completed tasks are included in the result
     * @return list of matching tasks
     */
    @Transactional(readOnly = true)
    public List<Task> findAll(UUID projectId, UUID sectionId, String label, String filter, boolean showCompleted) {
        if (filter != null) {
            ZoneId calendarZone = taskaProperties.getCalendar().getTimeZone();
            LocalDate today = LocalDate.now(calendarZone);
            Instant startOfToday = today.atStartOfDay(calendarZone).toInstant();
            Instant startOfTomorrow = today.plusDays(1).atStartOfDay(calendarZone).toInstant();
            return switch (filter) {
                case "today" ->
                        taskRepository.findByScheduledAtBetweenAndIsCompletedFalseOrderByScheduledAtAsc(startOfToday, startOfTomorrow);
                case "overdue" ->
                        taskRepository.findByScheduledAtBeforeAndIsCompletedFalseAndIsRecurringFalseOrderByScheduledAtAsc(startOfToday);
                case "upcoming" -> taskRepository.findByScheduledAtBetweenAndIsCompletedFalseOrderByScheduledAtAsc(
                        startOfTomorrow, today.plusDays(14).atStartOfDay(calendarZone).toInstant());
                default -> taskRepository.findAll();
            };
        }
        if (label != null) {
            return showCompleted ? taskRepository.findByLabel(label) : taskRepository.findByLabelAndIsCompletedFalse(label);
        }
        if (projectId != null && sectionId != null) {
            return showCompleted
                    ? taskRepository.findByProjectIdAndSectionIdOrderByPositionAsc(projectId, sectionId)
                    : taskRepository.findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(projectId, sectionId);
        }
        if (projectId != null) {
            return showCompleted
                    ? taskRepository.findByProjectIdOrderByPositionAsc(projectId)
                    : taskRepository.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
        }
        if (sectionId != null) {
            return showCompleted
                    ? taskRepository.findBySectionIdOrderByPositionAsc(sectionId)
                    : taskRepository.findBySectionIdAndIsCompletedFalseOrderByPositionAsc(sectionId);
        }
        return taskRepository.findAll();
    }

    /**
     * Returns all task occurrences (both regular and recurring) that fall within the given date range.
     * For recurring tasks, virtual occurrences are generated from the RRULE, with SKIPPED instances
     * excluded and MODIFIED instances merged in. Non-recurring tasks are included when their due date
     * falls inside the period.
     *
     * @param from start of the date range (inclusive, UTC)
     * @param to   end of the date range (inclusive, UTC)
     * @return list of task DTOs, each representing a single occurrence
     */
    @Transactional(readOnly = true)
    public List<TaskDto> findOccurrencesForDateRange(LocalDate from, LocalDate to) {
        return findOccurrencesForDateRange(from, to, false);
    }

    /**
     * Returns occurrences in the requested range, optionally retaining completed non-recurring tasks.
     * Completed recurring occurrences are already represented by their task instances.
     */
    public List<TaskDto> findOccurrencesForDateRange(LocalDate from, LocalDate to, boolean showCompleted) {
        ZoneId calendarZone = taskaProperties.getCalendar().getTimeZone();
        Instant periodStart = from.atStartOfDay(calendarZone).toInstant();
        Instant periodEnd = to.plusDays(1).atStartOfDay(calendarZone).toInstant();

        List<Task> nonRecurring = showCompleted
                ? taskRepository.findNonRecurringTasksIncludingCompletedInPeriod(periodStart, periodEnd)
                : taskRepository.findNonRecurringTasksInPeriod(periodStart, periodEnd);
        List<TaskDto> result = new ArrayList<>(nonRecurring.stream().map(taskMapper::toDto).toList());

        List<Task> recurringTasks = taskRepository.findActiveRecurringTasksForPeriod(periodStart, periodEnd);
        if (recurringTasks.isEmpty()) return result;

        List<UUID> ids = recurringTasks.stream().map(Task::getId).toList();

        // Instances whose occurrenceScheduledAt falls within the period (for RRULE occurrence matching).
        Map<UUID, Map<Instant, TaskInstance>> instancesByTask =
                taskInstanceRepository.findByTaskIdInAndOccurrenceScheduledAtBetween(ids, periodStart, periodEnd)
                        .stream()
                        .collect(Collectors.groupingBy(
                                TaskInstance::getTaskId,
                                Collectors.toMap(TaskInstance::getOccurrenceScheduledAt, i -> i, (a, b) -> a)
                        ));

        // MODIFIED instances whose scheduledAt was moved into this period from another day.
        Map<UUID, List<TaskInstance>> movedInByTask =
                taskInstanceRepository.findByTaskIdInAndStatusAndScheduledAtBetween(
                                ids, TaskInstanceStatus.MODIFIED, periodStart, periodEnd)
                        .stream()
                        .filter(i -> i.getOccurrenceScheduledAt().isBefore(periodStart)
                                || !i.getOccurrenceScheduledAt().isBefore(periodEnd))
                        .collect(Collectors.groupingBy(TaskInstance::getTaskId));

        for (Task task : recurringTasks) {
            Map<Instant, TaskInstance> taskInstances = instancesByTask.getOrDefault(task.getId(), Map.of());
            List<Instant> occurrences = recurrenceService.getOccurrencesInRange(task, periodStart, periodEnd);

            for (Instant occurrenceScheduledAt : occurrences) {
                TaskInstance instance = taskInstances.get(occurrenceScheduledAt);
                if (instance != null && instance.getStatus() == TaskInstanceStatus.SKIPPED) {
                    continue;
                }
                // Skip occurrences whose scheduledAt was moved outside this period.
                if (instance != null && instance.getScheduledAt() != null
                        && (instance.getScheduledAt().isBefore(periodStart)
                            || !instance.getScheduledAt().isBefore(periodEnd))) {
                    continue;
                }
                result.add(taskMapper.toOccurrenceDto(task, instance, occurrenceScheduledAt));
            }

            // Add occurrences that were rescheduled into this period from a different day.
            for (TaskInstance movedIn : movedInByTask.getOrDefault(task.getId(), List.of())) {
                result.add(taskMapper.toOccurrenceDto(task, movedIn, movedIn.getOccurrenceScheduledAt()));
            }
        }

        return result;
    }

    /**
     * Returns the task with the given ID, or throws {@link com.taska.exception.ResourceNotFoundException}
     * if no such task exists.
     *
     * @param taskId the task UUID
     * @return the matching task entity
     */
    @Transactional(readOnly = true)
    public Task findById(UUID taskId) {
        return getOrThrow(taskId);
    }

    /**
     * Creates and persists a new task from the given request.
     * If no {@code projectId} is provided and the task has no parent, it is placed in the inbox project.
     * Defaults: position 0, not recurring, and not all-day. Manual priority remains absent when
     * the request does not provide one.
     * The recurrence rule is normalised from short aliases (e.g. "daily" → "FREQ=DAILY").
     *
     * @param taskRequest the task creation payload
     * @return the persisted task entity
     */
    public Task create(TaskRequest taskRequest) {
        Task t = new Task();
        t.setContent(taskRequest.content());
        t.setType(taskRequest.type() != null ? taskRequest.type() : TaskType.TODO);
        t.setDescription(taskRequest.description());
        t.setSectionId(taskRequest.sectionId());
        t.setParentId(taskRequest.parentId());
        t.setPosition(taskRequest.order() != null ? taskRequest.order() : 0);
        t.setPriority(taskRequest.priority());
        t.setLabels(taskRequest.labels() != null ? taskRequest.labels() : new ArrayList<>());
        t.setScheduledAt(taskRequest.scheduledAt());
        t.setDueAt(taskRequest.dueAt());
        t.setAllDay(taskRequest.allDay() != null ? taskRequest.allDay() : false);
        t.setIsRecurring(taskRequest.isRecurring() != null ? taskRequest.isRecurring() : false);
        t.setEstimateMinutes(taskRequest.estimateMinutes());
        t.setMentionContext(taskRequest.mentionContext());
        t.setRecurrenceRule(normalizeRRule(taskRequest.recurrenceRule()));

        UUID projectId = taskRequest.projectId();
        if (projectId == null && taskRequest.parentId() == null) {
            projectId = projectRepository.findByIsInboxProjectTrue()
                    .orElseThrow(() -> new ResourceNotFoundException("Inbox project not found"))
                    .getId();
        }
        t.setProjectId(projectId);
        assertScheduleAllowed(projectId, t.getScheduledAt(), t.isAllDay());

        return taskRepository.save(t);
    }

    /**
     * Updates an existing task. The update strategy depends on whether the task is recurring and
     * whether a {@code scope} is provided:
     * <ul>
     *   <li><b>No scope (or non-recurring task)</b> – a standard patch is applied directly to the
     *       task entity; only non-null request fields overwrite existing values.</li>
     *   <li><b>{@code THIS_ONLY}</b> – creates or updates a {@link TaskInstance} for the specified
     *       occurrence, overriding only {@code content}, {@code priority}, {@code scheduledAt}, and {@code dueAt};
     *       all other occurrences remain unchanged.</li>
     *   <li><b>{@code FROM_THIS}</b> – truncates the original series one second before
     *       {@code occurrenceScheduledAt}, then creates a new recurring task starting at that instant
     *       with the requested changes applied.</li>
     * </ul>
     *
     * @param taskId      the task UUID to update
     * @param taskRequest the update payload; when {@code scope} is set, {@code occurrenceScheduledAt} must
     *                    also be provided to identify the target occurrence
     * @return the updated task (or occurrence) as a DTO
     * @throws IllegalArgumentException  if {@code scope} is set but {@code occurrenceScheduledAt} is {@code null}
     * @throws ResourceNotFoundException if {@code THIS_ONLY} is requested but {@code occurrenceScheduledAt}
     *                                   does not match any occurrence generated by the task's RRULE
     */
    public TaskDto update(UUID taskId, TaskRequest taskRequest) {
        return update(taskId, taskRequest, taskRequest.priority() != null);
    }

    /**
     * Updates a task while retaining JSON field-presence information for nullable patch fields.
     * {@code priorityProvided} distinguishes an omitted priority from an explicit JSON null: the
     * former leaves manual priority unchanged, while the latter clears it.
     *
     * @param taskId task to update
     * @param taskRequest parsed task fields
     * @param priorityProvided whether the caller explicitly supplied the priority field
     * @return the updated task or occurrence
     */
    public TaskDto update(UUID taskId, TaskRequest taskRequest, boolean priorityProvided) {

        // Input validation.
        if (taskRequest.scope() != null && taskRequest.occurrenceScheduledAt() == null) {
            throw new IllegalArgumentException("occurrenceScheduledAt is required when scope is provided");
        }

        Task task = getOrThrow(taskId);

        // Simple patch is applied for non-recurring tasks or when scope is null.
        if (taskRequest.scope() == null || !Boolean.TRUE.equals(task.getIsRecurring())) {
            applyPatch(task, taskRequest, priorityProvided);
            Task saved = taskRepository.save(task);
            priorityEvaluationRepository.deleteByTaskId(taskId);
            return taskMapper.toDto(saved);
        }

        // Recurring task update logic.
        Instant occurrenceScheduledAt = taskRequest.occurrenceScheduledAt();
        return switch (taskRequest.scope()) {
            case THIS_ONLY -> {
                validateOccurrence(task, occurrenceScheduledAt);
                TaskInstance instance = taskInstanceRepository
                        .findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
                        .orElseGet(TaskInstance::new);
                instance.setTaskId(taskId);
                instance.setOccurrenceScheduledAt(occurrenceScheduledAt);
                instance.setStatus(TaskInstanceStatus.MODIFIED);
                if (taskRequest.content() != null) instance.setTitle(taskRequest.content());
                if (priorityProvided && taskRequest.priority() != null) instance.setPriority(taskRequest.priority());
                if (taskRequest.scheduledAt() != null) instance.setScheduledAt(taskRequest.scheduledAt());
                if (taskRequest.scheduledAt() != null) assertScheduleAllowed(task.getProjectId(), taskRequest.scheduledAt(), task.isAllDay());
                if (taskRequest.dueAt() != null) instance.setDueAt(taskRequest.dueAt());
                yield taskMapper.toOccurrenceDto(task, taskInstanceRepository.save(instance), occurrenceScheduledAt);
            }
            case FROM_THIS -> {
                task.setRruleEndsAt(occurrenceScheduledAt.minus(1, ChronoUnit.SECONDS));
                taskRepository.save(task);

                Task cloned = new Task();
                cloned.setContent(taskRequest.content() != null ? taskRequest.content() : task.getContent());
                cloned.setType(taskRequest.type() != null ? taskRequest.type() : task.getType());
                cloned.setDescription(taskRequest.description() != null ? taskRequest.description() : task.getDescription());
                cloned.setProjectId(task.getProjectId());
                cloned.setSectionId(task.getSectionId());
                cloned.setParentId(task.getParentId());
                cloned.setPosition(task.getPosition());
                cloned.setPriority(priorityProvided ? taskRequest.priority() : task.getPriority());
                cloned.setLabels(taskRequest.labels() != null ? taskRequest.labels() : task.getLabels());
                cloned.setScheduledAt(occurrenceScheduledAt);
                cloned.setDueAt(taskRequest.dueAt() != null ? taskRequest.dueAt() : task.getDueAt());
                cloned.setAllDay(task.isAllDay());
                cloned.setIsRecurring(true);
                cloned.setEstimateMinutes(taskRequest.estimateMinutes() != null ? taskRequest.estimateMinutes() : task.getEstimateMinutes());
                cloned.setRecurrenceRule(taskRequest.recurrenceRule() != null ? taskRequest.recurrenceRule() : task.getRecurrenceRule());
                yield taskMapper.toDto(taskRepository.save(cloned));
            }
        };
    }

    /**
     * Deletes a task. The deletion strategy depends on the provided scope:
     * <ul>
     *   <li><b>No scope (or non-recurring task)</b> – the task entity is permanently deleted.</li>
     *   <li><b>{@code THIS_ONLY}</b> – marks the specified occurrence as SKIPPED by creating a
     *       {@link TaskInstance}; the rest of the series remains intact.</li>
     *   <li><b>{@code FROM_THIS}</b> – truncates the series one second before {@code occurrenceScheduledAt}
     *       so that no occurrences are generated from that point onwards.</li>
     * </ul>
     *
     * @param taskId            the task UUID to delete
     * @param taskDeleteRequest optional delete request containing the scope and the scheduled occurrence
     *                          instant; when {@code null} or when scope is {@code null}, the task is
     *                          permanently deleted regardless of whether it is recurring
     * @throws ResourceNotFoundException if {@code THIS_ONLY} is requested but {@code occurrenceScheduledAt}
     *                                   does not match any occurrence generated by the task's RRULE
     * @throws IllegalArgumentException  if {@code THIS_ONLY} is requested but the occurrence is already skipped
     * @throws IllegalStateException     if {@code THIS_ONLY} is requested but the occurrence is already completed
     *                                   (reopen it first before skipping)
     */
    public void delete(UUID taskId, TaskDeleteRequest taskDeleteRequest) {
        Task task = getOrThrow(taskId);

        if (taskDeleteRequest == null || taskDeleteRequest.scope() == null || !Boolean.TRUE.equals(task.getIsRecurring())) {
            priorityEvaluationRepository.deleteByTaskId(taskId);
            taskRepository.delete(task);
            return;
        }

        switch (taskDeleteRequest.scope()) {
            case THIS_ONLY -> {
                validateOccurrence(task, taskDeleteRequest.occurrenceScheduledAt());
                TaskInstance taskInstance = taskInstanceRepository
                        .findByTaskIdAndOccurrenceScheduledAt(taskId, taskDeleteRequest.occurrenceScheduledAt())
                        .orElseGet(TaskInstance::new);

                if (TaskInstanceStatus.SKIPPED.equals(taskInstance.getStatus())) {
                    throw new IllegalArgumentException("Occurrence " + taskInstance.getId() + " already skipped");
                }

                if (taskInstance.getId() != null && taskInstance.getStatus() == TaskInstanceStatus.DONE) {
                    throw new IllegalStateException(
                            "Cannot skip an already-completed occurrence (" + taskInstance.getId() + "). Reopen it first.");
                }

                taskInstance.setTaskId(taskId);
                taskInstance.setOccurrenceScheduledAt(taskDeleteRequest.occurrenceScheduledAt());
                taskInstance.setStatus(TaskInstanceStatus.SKIPPED);
                taskInstanceRepository.save(taskInstance);
            }
            case FROM_THIS -> {
                task.setRruleEndsAt(taskDeleteRequest.occurrenceScheduledAt().minus(1, ChronoUnit.SECONDS));
                taskRepository.save(task);
            }
        }
    }

    /**
     * Marks a task as completed. For non-recurring tasks the task entity is flagged as completed
     * with the current timestamp. For recurring tasks, a {@link TaskInstance} with status DONE is
     * created for the given occurrence.
     *
     * @param taskId                 the task UUID
     * @param taskCloseReopenRequest request containing the scheduled occurrence instant; required
     *                               for recurring tasks
     * @return the updated task as a DTO
     * @throws IllegalArgumentException  if the task is recurring and {@code occurrenceScheduledAt} is not provided
     * @throws ResourceNotFoundException if {@code occurrenceScheduledAt} does not match any occurrence generated
     *                                   by the task's RRULE
     * @throws IllegalArgumentException  if the specified recurring occurrence is already completed
     */
    public TaskDto close(UUID taskId, TaskCloseReopenRequest taskCloseReopenRequest) {
        Task task = getOrThrow(taskId);
        Instant occurrenceScheduledAt = taskCloseReopenRequest != null ? taskCloseReopenRequest.occurrenceScheduledAt() : null;

        // Validate input.
        if (Boolean.TRUE.equals(task.getIsRecurring()) && occurrenceScheduledAt == null) {
            throw new IllegalArgumentException("occurrenceScheduledAt is required to complete a recurring occurrence");
        }

        // Non-recurring tasks are completed by setting the completion flag and timestamp.
        if (!Boolean.TRUE.equals(task.getIsRecurring()) || occurrenceScheduledAt == null) {
            task.setIsCompleted(true);
            task.setCompletedAt(Instant.now());
            Task saved = taskRepository.save(task);
            priorityEvaluationRepository.deleteByTaskId(taskId);
            return taskMapper.toDto(saved);
        }

        // For recurring tasks, a TaskInstance is created or updated with status DONE for the given occurrence.
        validateOccurrence(task, occurrenceScheduledAt);
        TaskInstance instance = taskInstanceRepository
                .findByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt)
                .orElseGet(TaskInstance::new);
        if (instance.getId() != null && instance.getStatus() == TaskInstanceStatus.DONE) {
            throw new IllegalArgumentException("Occurrence already completed: " + occurrenceScheduledAt);
        }
        instance.setTaskId(taskId);
        instance.setOccurrenceScheduledAt(occurrenceScheduledAt);
        instance.setStatus(TaskInstanceStatus.DONE);
        instance.setCompletedAt(Instant.now());

        return taskMapper.toOccurrenceDto(task, taskInstanceRepository.save(instance), occurrenceScheduledAt);
    }

    /**
     * Reopens a previously completed task. For non-recurring tasks the completion flag and timestamp
     * are cleared. For recurring tasks, the {@link TaskInstance} representing the completed occurrence
     * is deleted, restoring it to its virtual (open) state.
     *
     * @param taskId the task UUID
     * @param taskCloseReopenRequest   optional request containing the scheduled occurrence instant for recurring tasks
     * @return the updated task as a DTO
     */
    public TaskDto reopen(UUID taskId, TaskCloseReopenRequest taskCloseReopenRequest) {
        Task task = getOrThrow(taskId);
        Instant occurrenceScheduledAt = taskCloseReopenRequest != null ? taskCloseReopenRequest.occurrenceScheduledAt() : null;

        if (!Boolean.TRUE.equals(task.getIsRecurring()) || occurrenceScheduledAt == null) {
            task.setIsCompleted(false);
            task.setCompletedAt(null);
            return taskMapper.toDto(taskRepository.save(task));
        }

        taskInstanceRepository.deleteByTaskIdAndOccurrenceScheduledAt(taskId, occurrenceScheduledAt);
        return taskMapper.toOccurrenceDto(task, null, occurrenceScheduledAt);
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
     * Loads a task by ID or throws {@link com.taska.exception.ResourceNotFoundException} if not found.
     *
     * @param taskId the task UUID
     * @return the task entity
     */
    public Task getOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    /**
     * Returns all incomplete, non-notified, non-all-day tasks whose due date is at or before
     * the given instant. Used by the notification scheduler to find tasks due in approximately
     * 15 minutes.
     *
     * @param instant the upper-bound instant (typically now + 15 minutes)
     * @return list of tasks eligible for a push notification
     */
    public List<Task> findTasksDueAround(Instant instant) {
        return taskRepository.findTasksDueAround(instant);
    }

    /**
     * Validates that {@code occurrenceScheduledAt} corresponds to a real occurrence generated by the
     * task's RRULE. The check is performed by expanding the RRULE over the full day that
     * contains {@code occurrenceScheduledAt} and verifying that the exact instant is present.
     *
     * @param task        the recurring task whose RRULE is checked
     * @param occurrenceScheduledAt the candidate occurrence instant
     * @throws ResourceNotFoundException if {@code occurrenceScheduledAt} does not match any computed occurrence
     */
    private void validateOccurrence(Task task, Instant occurrenceScheduledAt) {
        Instant dayStart = occurrenceScheduledAt.truncatedTo(ChronoUnit.DAYS);
        Instant dayEnd = dayStart.plus(1, ChronoUnit.DAYS);
        List<Instant> occurrences = recurrenceService.getOccurrencesInRange(task, dayStart, dayEnd);
        if (!occurrences.contains(occurrenceScheduledAt)) {
            throw new ResourceNotFoundException(
                    "No occurrence at " + occurrenceScheduledAt + " for task " + task.getId());
        }
    }

    /**
     * Converts shorthand recurrence aliases to valid iCal4j RRULE strings.
     * If {@code recurrenceStringRule} is already a valid RRULE (starts with "FREQ="), it is returned unchanged.
     * Null and blank inputs are passed through as-is.
     *
     * @param recurrenceStringRule raw recurrence rule string (e.g. "daily", "FREQ=WEEKLY")
     * @return normalised RRULE string or the original {@code recurrenceStringRule}
     */
    private static String normalizeRRule(String recurrenceStringRule) {
        if (recurrenceStringRule == null || recurrenceStringRule.isBlank() || recurrenceStringRule.toUpperCase().startsWith("FREQ=")) return recurrenceStringRule;
        return switch (recurrenceStringRule.toLowerCase()) {
            case "daily" -> "FREQ=DAILY";
            case "weekly" -> "FREQ=WEEKLY";
            case "monthly" -> "FREQ=MONTHLY";
            case "yearly" -> "FREQ=YEARLY";
            default -> recurrenceStringRule;
        };
    }

    /**
     * Applies non-null fields from the request onto an existing task entity.
     * When the planned scheduled time changes, the notification flag is reset so the task
     * can trigger a new push notification at its new time.
     *
     * @param task the task entity to mutate in-place
     * @param taskRequest  the update payload; only non-null fields are applied
     */
    private void applyPatch(Task task, TaskRequest taskRequest, boolean priorityProvided) {
        if (taskRequest.content() != null) task.setContent(taskRequest.content());
        if (taskRequest.type() != null) task.setType(taskRequest.type());
        if (taskRequest.description() != null) task.setDescription(taskRequest.description());
        if (taskRequest.projectId() != null) task.setProjectId(taskRequest.projectId());
        if (taskRequest.sectionId() != null) task.setSectionId(taskRequest.sectionId());
        if (taskRequest.parentId() != null) task.setParentId(taskRequest.parentId());
        if (taskRequest.order() != null) task.setPosition(taskRequest.order());
        if (priorityProvided) task.setPriority(taskRequest.priority());
        if (taskRequest.labels() != null) task.setLabels(taskRequest.labels());
        if (taskRequest.allDay() != null) task.setAllDay(taskRequest.allDay());
        if (taskRequest.isRecurring() != null) task.setIsRecurring(taskRequest.isRecurring());
        if (taskRequest.estimateMinutes() != null) task.setEstimateMinutes(taskRequest.estimateMinutes());
        if (taskRequest.mentionContext() != null) task.setMentionContext(taskRequest.mentionContext());
        if (taskRequest.recurrenceRule() != null) task.setRecurrenceRule(normalizeRRule(taskRequest.recurrenceRule()));
        if (taskRequest.dueAt() != null) task.setDueAt(taskRequest.dueAt());
        if (taskRequest.scheduledAt() != null) {
            UUID effectiveProjectId = taskRequest.projectId() != null ? taskRequest.projectId() : task.getProjectId();
            boolean effectiveAllDay = taskRequest.allDay() != null ? taskRequest.allDay() : task.isAllDay();
            assertScheduleAllowed(effectiveProjectId, taskRequest.scheduledAt(), effectiveAllDay);
            if (!taskRequest.scheduledAt().equals(task.getScheduledAt())) {
                task.setIsNotified(false);
            }
            task.setScheduledAt(taskRequest.scheduledAt());
        }
    }

    private void assertScheduleAllowed(UUID projectId, Instant scheduledAt, boolean allDay) {
        if (scheduledAt == null || projectId == null) return;
        UUID calendarId = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId))
                .getPlanningCalendarId();
        if (!planningCalendarService.allows(calendarId, scheduledAt, allDay)) {
            throw new IllegalArgumentException("Scheduled time is outside the project's planning calendar availability");
        }
    }
}
