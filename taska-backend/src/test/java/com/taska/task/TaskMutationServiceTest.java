package com.taska.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.task.application.TaskMutationService;
import com.taska.task.application.definition.TaskDefinitionService;
import com.taska.task.application.occurrence.RecurringTaskSeriesService;
import com.taska.task.application.occurrence.TaskOccurrenceService;
import com.taska.task.model.RecurrenceScope;
import com.taska.task.model.Task;
import com.taska.task.model.TaskChangedEvent;
import com.taska.task.model.TaskCloseReopenParameters;
import com.taska.task.model.TaskCreateParameters;
import com.taska.task.model.TaskDeleteParameters;
import com.taska.task.model.TaskOccurrenceUpdateParameters;
import com.taska.task.model.TaskPatchParameters;
import com.taska.task.model.TaskResult;
import com.taska.task.model.TaskType;
import com.taska.task.model.TaskUpdateParameters;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Verifies transport-contract routing and shared publication at the mutation boundary.
 */
class TaskMutationServiceTest {

    private final TaskDefinitionService taskService = mock(TaskDefinitionService.class);
    private final TaskOccurrenceService taskOccurrenceService = mock(TaskOccurrenceService.class);
    private final RecurringTaskSeriesService recurringTaskSeriesService = mock(RecurringTaskSeriesService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final TaskMutationService taskMutationService = new TaskMutationService(
            taskService,
            taskOccurrenceService,
            recurringTaskSeriesService,
            events);

    private Task task(boolean recurring) {
        Task task = new Task();
        task.setIsRecurring(recurring);
        return task;
    }

    private TaskPatchParameters patch(RecurrenceScope scope, Instant occurrenceScheduledAt) {
        return new TaskPatchParameters(
                "Updated",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                scope,
                occurrenceScheduledAt,
                null);
    }

    @Test
    void createAppliesTheMutationAndPublishesForEveryTransport() {
        TaskCreateParameters parameters = new TaskCreateParameters(
                "Task",
                null,
                null,
                null,
                0,
                null,
                List.of(),
                null,
                null,
                false,
                false,
                null,
                null,
                null,
                TaskType.TODO);
        Task task = new Task();
        when(taskService.create(parameters)).thenReturn(task);

        assertThat(taskMutationService.create(parameters, "account-a")).isSameAs(task);

        verify(taskService).create(parameters);
        verify(events).publishEvent(new TaskChangedEvent("account-a"));
    }

    @Test
    void unscopedUpdateRoutesToTaskOperation() {
        UUID id = UUID.randomUUID();
        var parameters = patch(null, null);
        var result = TaskResult.base(task(false));
        when(taskService.updateTask(id, parameters, false)).thenReturn(result);
        assertThat(taskMutationService.update(id, parameters, false, "account")).isSameAs(result);
        verify(events).publishEvent(new TaskChangedEvent("account"));
    }

    @Test
    void followingUpdateIsRejectedBeforeAnyMutationOrPublication() {
        var parameters = patch(RecurrenceScope.FROM_THIS, Instant.parse("2026-05-20T10:00:00Z"));
        assertThatThrownBy(() -> taskMutationService.update(UUID.randomUUID(), parameters, false, "account"))
                .isInstanceOf(IllegalArgumentException.class);
        org.mockito.Mockito.verifyNoInteractions(taskService, taskOccurrenceService, recurringTaskSeriesService, events);
    }

    @Test
    void occurrenceReschedulingDelegatesAndPublishesOnce() {
        UUID id = UUID.randomUUID();
        Instant anchor = Instant.parse("2026-05-20T10:00:00Z");
        var parameters = new TaskOccurrenceUpdateParameters(anchor.plusSeconds(3600));
        var result = TaskResult.base(task(true));
        when(taskOccurrenceService.replaceOccurrence(id, anchor, parameters)).thenReturn(result);
        assertThat(taskMutationService.replaceOccurrence(id, anchor, parameters, "account")).isSameAs(result);
        verify(events).publishEvent(new TaskChangedEvent("account"));
        org.mockito.Mockito.verifyNoInteractions(taskService, recurringTaskSeriesService);
    }

    @Test
    void commonSeriesUpdateDelegatesToDefinitionOwner() {
        UUID id = UUID.randomUUID();
        var parameters = patch(null, null);
        var result = TaskResult.base(task(true));
        when(taskService.updateTask(id, parameters, false)).thenReturn(result);
        assertThat(taskMutationService.update(id, parameters, false, "account")).isSameAs(result);
        verify(events).publishEvent(new TaskChangedEvent("account"));
    }

    @Test
    void rejectedPartialUpdateDoesNotPublish() {
        UUID id = UUID.randomUUID();
        var parameters = patch(null, null);
        when(taskService.updateTask(id, parameters, false)).thenThrow(new IllegalArgumentException("fixed generator"));
        assertThatThrownBy(() -> taskMutationService.update(id, parameters, false, "account")).isInstanceOf(IllegalArgumentException.class);
        verify(events, never()).publishEvent(any(TaskChangedEvent.class));
    }

    @Test
    void rejectedReplacementDoesNotPublish() {
        UUID id = UUID.randomUUID();
        var parameters = mock(TaskUpdateParameters.class);
        when(taskService.replace(id, parameters)).thenThrow(new IllegalArgumentException("fixed generator"));
        assertThatThrownBy(() -> taskMutationService.replace(id, parameters, "account")).isInstanceOf(IllegalArgumentException.class);
        verify(events, never()).publishEvent(any(TaskChangedEvent.class));
    }

    @Test
    void deleteRoutesToTaskSkipAndTruncateOperations() {
        UUID taskId = UUID.randomUUID();
        UUID seriesId = UUID.randomUUID();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        TaskDeleteParameters skipParameters = new TaskDeleteParameters(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);
        TaskDeleteParameters truncateParameters = new TaskDeleteParameters(RecurrenceScope.FROM_THIS, occurrenceScheduledAt);
        when(taskService.findById(taskId)).thenReturn(task(false));
        when(taskService.findById(seriesId)).thenReturn(task(true));

        taskMutationService.delete(taskId, skipParameters, "task-account");
        taskMutationService.delete(seriesId, skipParameters, "skip-account");
        taskMutationService.delete(seriesId, truncateParameters, "truncate-account");

        verify(taskService).deleteTask(taskId);
        verify(taskOccurrenceService).skipOccurrence(seriesId, occurrenceScheduledAt);
        verify(recurringTaskSeriesService).truncateSeriesFrom(seriesId, occurrenceScheduledAt);
        verify(events).publishEvent(new TaskChangedEvent("task-account"));
        verify(events).publishEvent(new TaskChangedEvent("skip-account"));
        verify(events).publishEvent(new TaskChangedEvent("truncate-account"));
    }

    @Test
    void closeRoutesToTaskAndOccurrenceOperations() {
        UUID taskId = UUID.randomUUID();
        UUID seriesId = UUID.randomUUID();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        Task task = task(false);
        Task series = task(true);
        TaskResult taskResult = TaskResult.base(task);
        TaskResult occurrenceResult = TaskResult.base(series);
        when(taskService.findById(taskId)).thenReturn(task);
        when(taskService.findById(seriesId)).thenReturn(series);
        when(taskService.closeTask(taskId)).thenReturn(taskResult);
        when(taskOccurrenceService.closeOccurrence(seriesId, occurrenceScheduledAt)).thenReturn(occurrenceResult);

        assertThat(taskMutationService.close(taskId, null, "task-account")).isSameAs(taskResult);
        assertThat(taskMutationService.close(seriesId, new TaskCloseReopenParameters(occurrenceScheduledAt), "occurrence-account"))
                .isSameAs(occurrenceResult);

        verify(taskService).closeTask(taskId);
        verify(taskOccurrenceService).closeOccurrence(seriesId, occurrenceScheduledAt);
        verify(events).publishEvent(new TaskChangedEvent("task-account"));
        verify(events).publishEvent(new TaskChangedEvent("occurrence-account"));
    }

    @Test
    void reopenRoutesToTaskAndOccurrenceOperations() {
        UUID taskId = UUID.randomUUID();
        UUID seriesId = UUID.randomUUID();
        Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
        Task task = task(false);
        Task series = task(true);
        TaskResult taskResult = TaskResult.base(task);
        TaskResult occurrenceResult = TaskResult.base(series);
        when(taskService.findById(taskId)).thenReturn(task);
        when(taskService.findById(seriesId)).thenReturn(series);
        when(taskService.reopenTask(taskId)).thenReturn(taskResult);
        when(taskOccurrenceService.reopenOccurrence(seriesId, occurrenceScheduledAt)).thenReturn(occurrenceResult);

        assertThat(taskMutationService.reopen(taskId, null, "task-account")).isSameAs(taskResult);
        assertThat(taskMutationService.reopen(seriesId, new TaskCloseReopenParameters(occurrenceScheduledAt), "occurrence-account"))
                .isSameAs(occurrenceResult);

        verify(taskService).reopenTask(taskId);
        verify(taskOccurrenceService).reopenOccurrence(seriesId, occurrenceScheduledAt);
        verify(events).publishEvent(new TaskChangedEvent("task-account"));
        verify(events).publishEvent(new TaskChangedEvent("occurrence-account"));
    }
}
