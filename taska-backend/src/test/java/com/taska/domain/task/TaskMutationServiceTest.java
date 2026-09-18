package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.notification.service.TaskChangePublisher;
import com.taska.domain.task.occurrence.RecurrenceScope;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.service.RecurringTaskSeriesService;
import com.taska.domain.task.service.TaskCloseReopenParameters;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskDeleteParameters;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskOccurrenceService;
import com.taska.domain.task.service.TaskOccurrenceUpdateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import com.taska.domain.task.service.TaskResult;
import com.taska.domain.task.service.TaskService;
import com.taska.domain.task.service.TaskUpdateParameters;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Verifies transport-contract routing and shared publication at the mutation boundary. */
class TaskMutationServiceTest {

  private final TaskService taskService = mock(TaskService.class);
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
    when(taskOccurrenceService.updateOccurrence(
            seriesId, occurrenceScheduledAt, occurrenceParameters, false))
        .thenReturn(result);
    when(recurringTaskSeriesService.updateSeriesFrom(
            seriesId, occurrenceScheduledAt, followingParameters, false))
        .thenReturn(result);

    assertThat(
            taskMutationService.update(seriesId, occurrenceParameters, false, "occurrence-account"))
        .isSameAs(result);
    assertThat(taskMutationService.update(seriesId, followingParameters, false, "series-account"))
        .isSameAs(result);

    verify(taskOccurrenceService)
        .updateOccurrence(seriesId, occurrenceScheduledAt, occurrenceParameters, false);
    verify(recurringTaskSeriesService)
        .updateSeriesFrom(seriesId, occurrenceScheduledAt, followingParameters, false);
    verify(taskChangePublisher).publishFor("occurrence-account");
    verify(taskChangePublisher).publishFor("series-account");
  }

  @Test
  void replacementOperationsDelegateToTheirOwningServicesAndPublishOnce() {
    UUID seriesId = UUID.randomUUID();
    Instant occurrenceScheduledAt = Instant.parse("2026-05-20T10:00:00Z");
    TaskUpdateParameters taskParameters = mock(TaskUpdateParameters.class);
    TaskOccurrenceUpdateParameters occurrenceParameters =
        mock(TaskOccurrenceUpdateParameters.class);
    TaskResult result = TaskResult.base(task(true));
    when(taskService.replace(seriesId, taskParameters)).thenReturn(result);
    when(recurringTaskSeriesService.replaceFollowing(
            seriesId, occurrenceScheduledAt, taskParameters))
        .thenReturn(result);
    when(taskOccurrenceService.replaceOccurrence(
            seriesId, occurrenceScheduledAt, occurrenceParameters))
        .thenReturn(result);

    assertThat(taskMutationService.replace(seriesId, taskParameters, "task-account"))
        .isSameAs(result);
    assertThat(
            taskMutationService.replaceFollowing(
                seriesId, occurrenceScheduledAt, taskParameters, "series-account"))
        .isSameAs(result);
    assertThat(
            taskMutationService.replaceOccurrence(
                seriesId, occurrenceScheduledAt, occurrenceParameters, "occurrence-account"))
        .isSameAs(result);

    verify(taskService).replace(seriesId, taskParameters);
    verify(recurringTaskSeriesService)
        .replaceFollowing(seriesId, occurrenceScheduledAt, taskParameters);
    verify(taskOccurrenceService)
        .replaceOccurrence(seriesId, occurrenceScheduledAt, occurrenceParameters);
    verify(taskChangePublisher).publishFor("task-account");
    verify(taskChangePublisher).publishFor("series-account");
    verify(taskChangePublisher).publishFor("occurrence-account");
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
