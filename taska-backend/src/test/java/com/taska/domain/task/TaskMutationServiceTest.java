package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.notification.service.TaskChangePublisher;
import com.taska.domain.task.definition.TaskType;
import com.taska.domain.task.definition.repository.Task;
import com.taska.domain.task.definition.service.TaskDefinitionService;
import com.taska.domain.task.occurrence.RecurrenceScope;
import com.taska.domain.task.occurrence.service.TaskOccurrenceService;
import com.taska.domain.task.occurrence.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.series.service.RecurringTaskSeriesService;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskDeleteParameters;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskUpdateParameters;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Verifies transport-contract routing and shared publication at the mutation boundary. */
class TaskMutationServiceTest {

  private final TaskDefinitionService taskService = mock(TaskDefinitionService.class);
  private final TaskOccurrenceService taskOccurrenceService = mock(TaskOccurrenceService.class);
  private final RecurringTaskSeriesService recurringTaskSeriesService =
      mock(RecurringTaskSeriesService.class);
  private final TaskChangePublisher taskChangePublisher = mock(TaskChangePublisher.class);
  private final TaskMutationService taskMutationService =
      new TaskMutationService(
          taskService, taskOccurrenceService, recurringTaskSeriesService, taskChangePublisher);

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
    TaskCreateParameters parameters =
        new TaskCreateParameters(
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
    verify(taskChangePublisher).publishFor("account-a");
  }

  @Test
  void unscopedUpdateRoutesToTaskOperation() {
    UUID id = UUID.randomUUID();
    var parameters = patch(null, null);
    var result = TaskResult.base(task(false));
    when(taskService.updateTask(id, parameters, false)).thenReturn(result);
    assertThat(taskMutationService.update(id, parameters, false, "account")).isSameAs(result);
    verify(taskChangePublisher).publishFor("account");
  }

  @Test
  void followingUpdateIsRejectedBeforeAnyMutationOrPublication() {
    var parameters = patch(RecurrenceScope.FROM_THIS, Instant.parse("2026-05-20T10:00:00Z"));
    assertThatThrownBy(
            () -> taskMutationService.update(UUID.randomUUID(), parameters, false, "account"))
        .isInstanceOf(IllegalArgumentException.class);
    org.mockito.Mockito.verifyNoInteractions(
        taskService, taskOccurrenceService, recurringTaskSeriesService, taskChangePublisher);
  }

  @Test
  void occurrenceReschedulingDelegatesAndPublishesOnce() {
    UUID id = UUID.randomUUID();
    Instant anchor = Instant.parse("2026-05-20T10:00:00Z");
    var parameters = new TaskOccurrenceUpdateParameters(anchor.plusSeconds(3600));
    var result = TaskResult.base(task(true));
    when(taskOccurrenceService.replaceOccurrence(id, anchor, parameters)).thenReturn(result);
    assertThat(taskMutationService.replaceOccurrence(id, anchor, parameters, "account"))
        .isSameAs(result);
    verify(taskChangePublisher).publishFor("account");
    org.mockito.Mockito.verifyNoInteractions(taskService, recurringTaskSeriesService);
  }

  @Test
  void commonSeriesUpdateDelegatesToDefinitionOwner() {
    UUID id = UUID.randomUUID();
    var parameters = patch(null, null);
    var result = TaskResult.base(task(true));
    when(taskService.updateTask(id, parameters, false)).thenReturn(result);
    assertThat(taskMutationService.update(id, parameters, false, "account")).isSameAs(result);
    verify(taskChangePublisher).publishFor("account");
  }

  @Test
  void rejectedPartialUpdateDoesNotPublish() {
    UUID id = UUID.randomUUID();
    var parameters = patch(null, null);
    when(taskService.updateTask(id, parameters, false))
        .thenThrow(new IllegalArgumentException("fixed generator"));
    assertThatThrownBy(() -> taskMutationService.update(id, parameters, false, "account"))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskChangePublisher, never()).publishFor(any());
  }

  @Test
  void rejectedReplacementDoesNotPublish() {
    UUID id = UUID.randomUUID();
    var parameters = mock(TaskUpdateParameters.class);
    when(taskService.replace(id, parameters))
        .thenThrow(new IllegalArgumentException("fixed generator"));
    assertThatThrownBy(() -> taskMutationService.replace(id, parameters, "account"))
        .isInstanceOf(IllegalArgumentException.class);
    verify(taskChangePublisher, never()).publishFor(any());
  }

  @Test
  void deleteRoutesToTaskSkipAndTruncateOperations() {
    UUID taskId = UUID.randomUUID();
    UUID seriesId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskDeleteParameters skipParameters =
        new TaskDeleteParameters(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);
    TaskDeleteParameters truncateParameters =
        new TaskDeleteParameters(RecurrenceScope.FROM_THIS, occurrenceScheduledAt);
    when(taskService.findById(taskId)).thenReturn(task(false));
    when(taskService.findById(seriesId)).thenReturn(task(true));

    taskMutationService.delete(taskId, skipParameters, "task-account");
    taskMutationService.delete(seriesId, skipParameters, "skip-account");
    taskMutationService.delete(seriesId, truncateParameters, "truncate-account");

    verify(taskService).deleteTask(taskId);
    verify(taskOccurrenceService).skipOccurrence(seriesId, occurrenceScheduledAt);
    verify(recurringTaskSeriesService).truncateSeriesFrom(seriesId, occurrenceScheduledAt);
    verify(taskChangePublisher).publishFor("task-account");
    verify(taskChangePublisher).publishFor("skip-account");
    verify(taskChangePublisher).publishFor("truncate-account");
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
    when(taskOccurrenceService.closeOccurrence(seriesId, occurrenceScheduledAt))
        .thenReturn(occurrenceResult);

    assertThat(taskMutationService.close(taskId, null, "task-account")).isSameAs(taskResult);
    assertThat(
            taskMutationService.close(
                seriesId,
                new TaskCloseReopenParameters(occurrenceScheduledAt),
                "occurrence-account"))
        .isSameAs(occurrenceResult);

    verify(taskService).closeTask(taskId);
    verify(taskOccurrenceService).closeOccurrence(seriesId, occurrenceScheduledAt);
    verify(taskChangePublisher).publishFor("task-account");
    verify(taskChangePublisher).publishFor("occurrence-account");
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
    when(taskOccurrenceService.reopenOccurrence(seriesId, occurrenceScheduledAt))
        .thenReturn(occurrenceResult);

    assertThat(taskMutationService.reopen(taskId, null, "task-account")).isSameAs(taskResult);
    assertThat(
            taskMutationService.reopen(
                seriesId,
                new TaskCloseReopenParameters(occurrenceScheduledAt),
                "occurrence-account"))
        .isSameAs(occurrenceResult);

    verify(taskService).reopenTask(taskId);
    verify(taskOccurrenceService).reopenOccurrence(seriesId, occurrenceScheduledAt);
    verify(taskChangePublisher).publishFor("task-account");
    verify(taskChangePublisher).publishFor("occurrence-account");
  }
}
