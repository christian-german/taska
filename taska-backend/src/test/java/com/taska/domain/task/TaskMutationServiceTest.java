package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.notification.service.TaskChangePublisher;
import com.taska.domain.task.occurrence.RecurrenceScope;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskDeleteParameters;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Verifies transport-contract routing and shared publication at the mutation boundary. */
class TaskMutationServiceTest {

  private final TaskService taskService = mock(TaskService.class);
  private final TaskChangePublisher taskChangePublisher = mock(TaskChangePublisher.class);
  private final TaskMutationService taskMutationService =
      new TaskMutationService(taskService, taskChangePublisher);

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
  void updateRoutesNonRecurringScopedRequestToTaskOperation() {
    UUID taskId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task task = task(false);
    TaskPatchParameters parameters = patch(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);
    TaskResult result = TaskResult.base(task);
    when(taskService.findById(taskId)).thenReturn(task);
    when(taskService.updateTask(taskId, parameters, false)).thenReturn(result);

    assertThat(taskMutationService.update(taskId, parameters, false, "account-a")).isSameAs(result);

    verify(taskService).updateTask(taskId, parameters, false);
    verify(taskChangePublisher).publishFor("account-a");
  }

  @Test
  void updateRoutesRecurringScopesToOccurrenceAndFollowingSeriesOperations() {
    UUID seriesId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    Task series = task(true);
    TaskPatchParameters occurrenceParameters =
        patch(RecurrenceScope.THIS_ONLY, occurrenceScheduledAt);
    TaskPatchParameters followingParameters =
        patch(RecurrenceScope.FROM_THIS, occurrenceScheduledAt);
    TaskResult result = TaskResult.base(series);
    when(taskService.findById(seriesId)).thenReturn(series);
    when(taskService.updateOccurrence(seriesId, occurrenceScheduledAt, occurrenceParameters, false))
        .thenReturn(result);
    when(taskService.updateSeriesFrom(seriesId, occurrenceScheduledAt, followingParameters, false))
        .thenReturn(result);

    assertThat(
            taskMutationService.update(seriesId, occurrenceParameters, false, "occurrence-account"))
        .isSameAs(result);
    assertThat(taskMutationService.update(seriesId, followingParameters, false, "series-account"))
        .isSameAs(result);

    verify(taskService)
        .updateOccurrence(seriesId, occurrenceScheduledAt, occurrenceParameters, false);
    verify(taskService)
        .updateSeriesFrom(seriesId, occurrenceScheduledAt, followingParameters, false);
    verify(taskChangePublisher).publishFor("occurrence-account");
    verify(taskChangePublisher).publishFor("series-account");
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
    verify(taskService).skipOccurrence(seriesId, occurrenceScheduledAt);
    verify(taskService).truncateSeriesFrom(seriesId, occurrenceScheduledAt);
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
    when(taskService.closeOccurrence(seriesId, occurrenceScheduledAt)).thenReturn(occurrenceResult);

    assertThat(taskMutationService.close(taskId, null, "task-account")).isSameAs(taskResult);
    assertThat(
            taskMutationService.close(
                seriesId,
                new TaskCloseReopenParameters(occurrenceScheduledAt),
                "occurrence-account"))
        .isSameAs(occurrenceResult);

    verify(taskService).closeTask(taskId);
    verify(taskService).closeOccurrence(seriesId, occurrenceScheduledAt);
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
    when(taskService.reopenOccurrence(seriesId, occurrenceScheduledAt))
        .thenReturn(occurrenceResult);

    assertThat(taskMutationService.reopen(taskId, null, "task-account")).isSameAs(taskResult);
    assertThat(
            taskMutationService.reopen(
                seriesId,
                new TaskCloseReopenParameters(occurrenceScheduledAt),
                "occurrence-account"))
        .isSameAs(occurrenceResult);

    verify(taskService).reopenTask(taskId);
    verify(taskService).reopenOccurrence(seriesId, occurrenceScheduledAt);
    verify(taskChangePublisher).publishFor("task-account");
    verify(taskChangePublisher).publishFor("occurrence-account");
  }
}
