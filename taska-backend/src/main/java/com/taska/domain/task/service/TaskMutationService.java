package com.taska.domain.task.service;

import com.taska.domain.notification.service.TaskChangePublisher;
import com.taska.domain.task.repository.Task;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Transport-independent mutation boundary that applies task changes and their shared side effects.
 *
 * <p>Existing HTTP and MCP contracts encode the mutation target through optional scope and
 * occurrence fields. This boundary translates those contracts to the explicit task, occurrence, or
 * following-series operations exposed by {@link TaskService}.
 */
@Service
@RequiredArgsConstructor
public class TaskMutationService {

  private final TaskService taskService;
  private final TaskChangePublisher taskChangePublisher;

  /**
   * Creates a task and publishes the account-level change signal.
   *
   * @param parameters task creation values
   * @param accountSubject account receiving the change signal
   * @return the created task
   */
  public Task create(TaskCreateParameters parameters, String accountSubject) {
    Task task = taskService.create(parameters);
    publishChange(accountSubject);
    return task;
  }

  /**
   * Routes the existing scoped patch contract to one explicit task, occurrence, or series
   * operation.
   *
   * @param taskId identifier supplied by the transport contract
   * @param parameters requested changes and optional recurrence scope
   * @param priorityProvided whether the caller explicitly supplied the priority field
   * @param accountSubject account receiving the change signal
   * @return the updated task, occurrence, or successor series
   * @throws IllegalArgumentException if a recurrence scope has no occurrence identity
   */
  public TaskResult update(
      UUID taskId,
      TaskPatchParameters parameters,
      boolean priorityProvided,
      String accountSubject) {
    if (parameters.scope() != null && parameters.occurrenceScheduledAt() == null) {
      throw new IllegalArgumentException(
          "occurrenceScheduledAt is required when scope is provided");
    }

    Task task = taskService.findById(taskId);
    TaskResult taskResult;
    if (parameters.scope() == null || !Boolean.TRUE.equals(task.getIsRecurring())) {
      taskResult = taskService.updateTask(taskId, parameters, priorityProvided);
    } else {
      taskResult =
          switch (parameters.scope()) {
            case THIS_ONLY ->
                taskService.updateOccurrence(
                    taskId, parameters.occurrenceScheduledAt(), parameters, priorityProvided);
            case FROM_THIS ->
                taskService.updateSeriesFrom(
                    taskId, parameters.occurrenceScheduledAt(), parameters, priorityProvided);
          };
    }
    publishChange(accountSubject);
    return taskResult;
  }

  /**
   * Replaces a stored task or recurring-series definition and publishes its change signal.
   *
   * @param taskId stored task or series identifier
   * @param parameters complete replacement values
   * @param accountSubject account receiving the change signal
   * @return the replaced task or series
   */
  public TaskResult replace(UUID taskId, TaskUpdateParameters parameters, String accountSubject) {
    TaskResult taskResult = taskService.replace(taskId, parameters);
    publishChange(accountSubject);
    return taskResult;
  }

  /**
   * Replaces a recurring series from one occurrence onward and publishes its change signal.
   *
   * @param taskId recurring-series identifier
   * @param occurrenceScheduledAt first occurrence represented by the successor series
   * @param parameters complete successor-series values
   * @param accountSubject account receiving the change signal
   * @return the successor series
   */
  public TaskResult replaceFollowing(
      UUID taskId,
      Instant occurrenceScheduledAt,
      TaskUpdateParameters parameters,
      String accountSubject) {
    TaskResult taskResult = taskService.replaceFollowing(taskId, occurrenceScheduledAt, parameters);
    publishChange(accountSubject);
    return taskResult;
  }

  /**
   * Replaces the overrides of one recurring occurrence and publishes its change signal.
   *
   * @param taskId recurring-series identifier
   * @param occurrenceScheduledAt stable schedule identity of the occurrence
   * @param parameters complete occurrence override values
   * @param accountSubject account receiving the change signal
   * @return the replaced occurrence
   */
  public TaskResult replaceOccurrence(
      UUID taskId,
      Instant occurrenceScheduledAt,
      TaskOccurrenceUpdateParameters parameters,
      String accountSubject) {
    TaskResult taskResult =
        taskService.replaceOccurrence(taskId, occurrenceScheduledAt, parameters);
    publishChange(accountSubject);
    return taskResult;
  }

  /**
   * Routes the existing scoped deletion contract to task deletion, occurrence skipping, or series
   * truncation.
   *
   * @param taskId identifier supplied by the transport contract
   * @param parameters optional recurrence deletion scope
   * @param accountSubject account receiving the change signal
   */
  public void delete(UUID taskId, TaskDeleteParameters parameters, String accountSubject) {
    Task task = taskService.findById(taskId);
    if (parameters == null
        || parameters.scope() == null
        || !Boolean.TRUE.equals(task.getIsRecurring())) {
      taskService.deleteTask(taskId);
    } else {
      switch (parameters.scope()) {
        case THIS_ONLY -> taskService.skipOccurrence(taskId, parameters.occurrenceScheduledAt());
        case FROM_THIS ->
            taskService.truncateSeriesFrom(taskId, parameters.occurrenceScheduledAt());
      }
    }
    publishChange(accountSubject);
  }

  /**
   * Routes the close contract to a task or occurrence operation and publishes its change signal.
   *
   * @param taskId identifier supplied by the transport contract
   * @param parameters optional recurring-occurrence identity
   * @param accountSubject account receiving the change signal
   * @return the completed task or occurrence
   */
  public TaskResult close(
      UUID taskId, TaskCloseReopenParameters parameters, String accountSubject) {
    Task task = taskService.findById(taskId);
    TaskResult taskResult =
        Boolean.TRUE.equals(task.getIsRecurring())
            ? taskService.closeOccurrence(
                taskId, parameters != null ? parameters.occurrenceScheduledAt() : null)
            : taskService.closeTask(taskId);
    publishChange(accountSubject);
    return taskResult;
  }

  /**
   * Routes the reopen contract to a task or occurrence operation and publishes its change signal.
   *
   * @param taskId identifier supplied by the transport contract
   * @param parameters optional recurring-occurrence identity
   * @param accountSubject account receiving the change signal
   * @return the reopened task or occurrence
   */
  public TaskResult reopen(
      UUID taskId, TaskCloseReopenParameters parameters, String accountSubject) {
    Task task = taskService.findById(taskId);
    TaskResult taskResult =
        Boolean.TRUE.equals(task.getIsRecurring())
            ? taskService.reopenOccurrence(
                taskId, parameters != null ? parameters.occurrenceScheduledAt() : null)
            : taskService.reopenTask(taskId);
    publishChange(accountSubject);
    return taskResult;
  }

  private void publishChange(String accountSubject) {
    taskChangePublisher.publishFor(accountSubject);
  }
}
