package com.taska.domain.task.definition.service;

import com.taska.domain.planningcalendar.service.PlanningCalendarService;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.project.repository.ProjectRepository;
import com.taska.domain.task.definition.TaskDefinitionRules;
import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.definition.repository.TaskRepository;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskUpdateParameters;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TaskDefinitionService {

  private final TaskRepository taskRepository;
  private final ProjectRepository projectRepository;
  private final TaskPriorityEvaluationRepository priorityEvaluationRepository;
  private final PlanningCalendarService planningCalendarService;

  /**
   * Returns a list of tasks scoped by the provided project or label criteria.
   *
   * @param projectId optional project ID to scope results
   * @param label optional label name to filter by
   * @param showCompleted when true, completed tasks are included in the result
   * @return list of matching tasks
   */
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
  @Transactional
  public Task create(TaskCreateParameters taskCreateParameters) {
    TaskDefinitionRules.assertDueAtAllowed(
        taskCreateParameters.recurring(), taskCreateParameters.dueAt());
    String createdRecurrenceRule =
        TaskDefinitionRules.normalizeRecurrenceRule(taskCreateParameters.recurrenceRule());
    TaskDefinitionRules.assertRecurrenceRuleRequired(
        taskCreateParameters.recurring(), createdRecurrenceRule);
    TaskDefinitionRules.assertScheduledAtRequired(
        taskCreateParameters.recurring(), taskCreateParameters.scheduledAt());
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
    task.setDueAt(taskCreateParameters.recurring() ? null : taskCreateParameters.dueAt());
    task.setAllDay(taskCreateParameters.allDay());
    task.setIsRecurring(taskCreateParameters.recurring());
    task.setEstimateMinutes(taskCreateParameters.estimateMinutes());
    task.setMentionContext(taskCreateParameters.mentionContext());
    task.setRecurrenceRule(createdRecurrenceRule);

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
   * Applies a partial update directly to a stored task or recurring-series definition.
   *
   * <p>{@code priorityProvided} retains JSON field-presence information: it distinguishes an
   * omitted priority from an explicit JSON null, the former leaving manual priority unchanged and
   * the latter clearing it.
   *
   * @param taskId the stored task or series identifier
   * @param taskPatchParameters the fields to update
   * @param priorityProvided whether the caller explicitly supplied the priority field
   * @return the updated task or recurring-series definition
   * @throws ResourceNotFoundException if the task does not exist
   */
  @Transactional
  public TaskResult updateTask(
      UUID taskId, TaskPatchParameters taskPatchParameters, boolean priorityProvided) {
    Task task = getOrThrow(taskId);
    applyPatch(task, taskPatchParameters, priorityProvided);
    Task saved = taskRepository.save(task);
    priorityEvaluationRepository.deleteByTaskId(taskId);
    return TaskResult.base(saved);
  }

  /**
   * Replaces every mutable field of a stored task or recurring-series definition.
   *
   * @param taskId stored task or series identifier
   * @param taskUpdateParameters complete replacement values
   * @return the replaced task or recurring-series definition
   * @throws ResourceNotFoundException if the task does not exist
   * @throws IllegalArgumentException if a recurring replacement contains an absolute deadline
   */
  @Transactional
  public TaskResult replace(UUID taskId, TaskUpdateParameters taskUpdateParameters) {
    Task task = getOrThrow(taskId);
    replaceMutableFields(task, taskUpdateParameters);
    Task saved = taskRepository.save(task);
    priorityEvaluationRepository.deleteByTaskId(taskId);
    return TaskResult.base(saved);
  }

  /**
   * Permanently deletes a stored task or recurring-series definition.
   *
   * @param taskId the stored task or series identifier
   * @throws ResourceNotFoundException if the task does not exist
   */
  @Transactional
  public void deleteTask(UUID taskId) {
    Task task = getOrThrow(taskId);
    priorityEvaluationRepository.deleteByTaskId(taskId);
    taskRepository.delete(task);
  }

  /**
   * Marks a non-recurring task as completed with the current timestamp.
   *
   * @param taskId the non-recurring task identifier
   * @return the completed task
   * @throws IllegalArgumentException if the identifier refers to a recurring series
   * @throws ResourceNotFoundException if the task does not exist
   */
  @Transactional
  public TaskResult closeTask(UUID taskId) {
    Task task = getOrThrow(taskId);
    if (Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required to complete a recurring occurrence");
    }
    task.setIsCompleted(true);
    task.setCompletedAt(Instant.now());
    Task saved = taskRepository.save(task);
    priorityEvaluationRepository.deleteByTaskId(taskId);
    return TaskResult.base(saved);
  }

  /**
   * Reopens a previously completed non-recurring task.
   *
   * @param taskId the non-recurring task identifier
   * @return the reopened task
   * @throws IllegalArgumentException if the identifier refers to a recurring series
   * @throws ResourceNotFoundException if the task does not exist
   */
  @Transactional
  public TaskResult reopenTask(UUID taskId) {
    Task task = getOrThrow(taskId);
    if (Boolean.TRUE.equals(task.getIsRecurring())) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required to reopen a recurring occurrence");
    }
    task.setIsCompleted(false);
    task.setCompletedAt(null);
    return TaskResult.base(taskRepository.save(task));
  }

  /**
   * Returns all direct subtasks of the given parent task, ordered by their position.
   *
   * @param parentTaskId the UUID of the parent task
   * @return list of subtask entities
   */
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
    boolean recurring =
        taskPatchParameters.recurring() != null
            ? taskPatchParameters.recurring()
            : Boolean.TRUE.equals(task.getIsRecurring());
    TaskDefinitionRules.assertDueAtAllowed(recurring, taskPatchParameters.dueAt());
    String patchedRecurrenceRule =
        taskPatchParameters.recurrenceRule() != null
            ? TaskDefinitionRules.normalizeRecurrenceRule(taskPatchParameters.recurrenceRule())
            : task.getRecurrenceRule();
    TaskDefinitionRules.assertRecurrenceRuleRequired(recurring, patchedRecurrenceRule);
    Instant patchedScheduledAt =
        taskPatchParameters.scheduledAt() != null
            ? taskPatchParameters.scheduledAt()
            : task.getScheduledAt();
    TaskDefinitionRules.assertScheduledAtRequired(recurring, patchedScheduledAt);
    TaskDefinitionRules.assertGeneratorUnchanged(
        task,
        recurring,
        patchedScheduledAt,
        patchedRecurrenceRule,
        taskPatchParameters.allDay() != null ? taskPatchParameters.allDay() : task.isAllDay());
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
      task.setIsRecurring(recurring);
    }
    if (taskPatchParameters.estimateMinutes() != null) {
      task.setEstimateMinutes(taskPatchParameters.estimateMinutes());
    }
    if (taskPatchParameters.mentionContext() != null) {
      task.setMentionContext(taskPatchParameters.mentionContext());
    }
    if (taskPatchParameters.recurrenceRule() != null) {
      task.setRecurrenceRule(
          TaskDefinitionRules.normalizeRecurrenceRule(taskPatchParameters.recurrenceRule()));
    }
    if (recurring) {
      // Converting a task to a series removes an absolute deadline inherited from the former task.
      task.setDueAt(null);
    } else if (taskPatchParameters.dueAt() != null) {
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

  /**
   * Applies the authoritative complete-replacement mapping to a task definition.
   *
   * @param task task definition to mutate
   * @param taskUpdateParameters complete replacement values
   */
  private void replaceMutableFields(Task task, TaskUpdateParameters taskUpdateParameters) {
    TaskDefinitionRules.assertGeneratorUnchanged(
        task,
        taskUpdateParameters.recurring(),
        taskUpdateParameters.scheduledAt(),
        taskUpdateParameters.recurrenceRule(),
        taskUpdateParameters.allDay());
    TaskDefinitionRules.assertDueAtAllowed(
        taskUpdateParameters.recurring(), taskUpdateParameters.dueAt());
    String replacementRecurrenceRule =
        TaskDefinitionRules.normalizeRecurrenceRule(taskUpdateParameters.recurrenceRule());
    TaskDefinitionRules.assertRecurrenceRuleRequired(
        taskUpdateParameters.recurring(), replacementRecurrenceRule);
    TaskDefinitionRules.assertScheduledAtRequired(
        taskUpdateParameters.recurring(), taskUpdateParameters.scheduledAt());
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
    task.setDueAt(taskUpdateParameters.recurring() ? null : taskUpdateParameters.dueAt());
    task.setAllDay(taskUpdateParameters.allDay());
    task.setIsRecurring(taskUpdateParameters.recurring());
    task.setEstimateMinutes(taskUpdateParameters.estimateMinutes());
    task.setMentionContext(taskUpdateParameters.mentionContext());
    task.setRecurrenceRule(replacementRecurrenceRule);
    assertScheduleAllowed(
        taskUpdateParameters.projectId(),
        taskUpdateParameters.scheduledAt(),
        taskUpdateParameters.allDay());
  }

  /**
   * Validates a task or occurrence schedule against its project's planning calendar.
   *
   * @param projectId project whose planning calendar applies
   * @param scheduledAt proposed schedule
   * @param allDay whether the schedule represents an all-day item
   * @throws ResourceNotFoundException when the project does not exist
   * @throws IllegalArgumentException when the proposed schedule is unavailable
   */
  public void assertScheduleAllowed(UUID projectId, Instant scheduledAt, boolean allDay) {
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
