package com.taska.domain.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.taska.domain.priority.controller.TaskPriorityEvaluationDto;
import com.taska.domain.priority.controller.TaskPriorityEvaluationMapper;
import com.taska.domain.priority.repository.TaskPriorityEvaluation;
import com.taska.domain.priority.service.TaskPriorityEvaluationService;
import com.taska.domain.task.controller.TaskController;
import com.taska.domain.task.controller.TaskMapper;
import com.taska.domain.task.controller.TaskUpdateRequest;
import com.taska.domain.task.service.TaskMutationService;
import com.taska.domain.task.service.TaskOccurrenceService;
import com.taska.domain.task.service.TaskService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

class TaskControllerPriorityEvaluationTest {
  private final TaskService taskService = mock(TaskService.class);
  private final TaskOccurrenceService taskOccurrenceService = mock(TaskOccurrenceService.class);
  private final TaskMapper taskMapper = mock(TaskMapper.class);
  private final TaskPriorityEvaluationService taskPriorityEvaluationService =
      mock(TaskPriorityEvaluationService.class);
  private final TaskPriorityEvaluationMapper priorityEvaluationMapper =
      mock(TaskPriorityEvaluationMapper.class);
  private final TaskMutationService taskMutationService = mock(TaskMutationService.class);
  private final TaskController taskController =
      new TaskController(
          taskService,
          taskOccurrenceService,
          taskMutationService,
          taskMapper,
          taskPriorityEvaluationService,
          priorityEvaluationMapper);

  @Test
  void returnsEvaluationWhenPresent() {
    UUID id = UUID.randomUUID();
    var entity = new TaskPriorityEvaluation();
    entity.setTaskId(id);
    entity.setScore(95);
    entity.setComponents(new ObjectMapper().createObjectNode());
    entity.setComputedAt(Instant.now());
    var evaluation =
        new TaskPriorityEvaluationDto(
            entity.getTaskId(), entity.getScore(), entity.getComponents(), entity.getComputedAt());
    when(taskPriorityEvaluationService.findForTask(id)).thenReturn(Optional.of(entity));
    when(priorityEvaluationMapper.toDto(entity)).thenReturn(evaluation);
    var responseEntity = taskController.getPriorityEvaluation(id);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    assertThat(responseEntity.getBody()).isEqualTo(evaluation);
  }

  @Test
  void returnsNoContentWhenEvaluationIsMissing() {
    UUID id = UUID.randomUUID();
    when(taskPriorityEvaluationService.findForTask(id)).thenReturn(Optional.empty());
    var responseEntity = taskController.getPriorityEvaluation(id);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(204);
    assertThat(responseEntity.getBody()).isNull();
  }

  @Test
  void taskUpdateRequest_rejectsPayloadsThatOmitMutableProperties() {
    assertThatThrownBy(
            () ->
                new JsonMapper().readValue("{\"content\":\"Only title\"}", TaskUpdateRequest.class))
        .isInstanceOf(MismatchedInputException.class);
  }
}
