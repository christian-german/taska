package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taska.domain.notification.service.TaskChangePublisher;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;

class TaskMutationServiceTest {

  private final TaskService taskService = mock(TaskService.class);
  private final TaskChangePublisher taskChangePublisher = mock(TaskChangePublisher.class);
  private final TaskMutationService taskMutationService =
      new TaskMutationService(taskService, taskChangePublisher);

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
}
