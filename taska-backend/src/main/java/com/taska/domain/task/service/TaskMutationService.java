package com.taska.domain.task.service;

import com.taska.domain.notification.service.TaskChangePublisher;
import com.taska.domain.task.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Transport-independent mutation boundary that applies task changes and their shared side effects.
 */
@Service
@RequiredArgsConstructor
public class TaskMutationService {

    private final TaskService taskService;
    private final TaskChangePublisher taskChangePublisher;

    public Task create(TaskCreateParameters parameters, String accountSubject) {
        Task task = taskService.create(parameters);
        publishChange(accountSubject);
        return task;
    }

    public TaskResult update(
            UUID taskId,
            TaskPatchParameters parameters,
            boolean priorityProvided,
            String accountSubject) {
        TaskResult taskResult = taskService.update(taskId, parameters, priorityProvided);
        publishChange(accountSubject);
        return taskResult;
    }

    public TaskResult replace(
            UUID taskId,
            TaskUpdateParameters parameters,
            String accountSubject) {
        TaskResult taskResult = taskService.replace(taskId, parameters);
        publishChange(accountSubject);
        return taskResult;
    }

    public TaskResult replaceFollowing(
            UUID taskId,
            Instant occurrenceScheduledAt,
            TaskUpdateParameters parameters,
            String accountSubject) {
        TaskResult taskResult = taskService.replaceFollowing(taskId, occurrenceScheduledAt, parameters);
        publishChange(accountSubject);
        return taskResult;
    }

    public TaskResult replaceOccurrence(
            UUID taskId,
            Instant occurrenceScheduledAt,
            TaskOccurrenceUpdateParameters parameters,
            String accountSubject) {
        TaskResult taskResult = taskService.replaceOccurrence(taskId, occurrenceScheduledAt, parameters);
        publishChange(accountSubject);
        return taskResult;
    }

    public void delete(UUID taskId, TaskDeleteParameters parameters, String accountSubject) {
        taskService.delete(taskId, parameters);
        publishChange(accountSubject);
    }

    public TaskResult close(
            UUID taskId,
            TaskCloseReopenParameters parameters,
            String accountSubject) {
        TaskResult taskResult = taskService.close(taskId, parameters);
        publishChange(accountSubject);
        return taskResult;
    }

    public TaskResult reopen(
            UUID taskId,
            TaskCloseReopenParameters parameters,
            String accountSubject) {
        TaskResult taskResult = taskService.reopen(taskId, parameters);
        publishChange(accountSubject);
        return taskResult;
    }

    private void publishChange(String accountSubject) {
        taskChangePublisher.publishFor(accountSubject);
    }
}
