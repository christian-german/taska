package com.taska.domain.task;

import com.taska.domain.priority.TaskPriorityEvaluationDto;
import com.taska.domain.priority.TaskPriorityEvaluationService;
import com.taska.domain.notification.TaskChangePublisher;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TaskControllerPriorityEvaluationTest {
    private final TaskService taskService = mock(TaskService.class);
    private final TaskMapper taskMapper = mock(TaskMapper.class);
    private final TaskPriorityEvaluationService taskPriorityEvaluationService = mock(TaskPriorityEvaluationService.class);
    private final TaskChangePublisher taskChangePublisher = mock(TaskChangePublisher.class);
    private final TaskController taskController = new TaskController(taskService, taskMapper, taskPriorityEvaluationService, taskChangePublisher);

    @Test
    void returnsEvaluationWhenPresent() {
        UUID id = UUID.randomUUID();
        var evaluation = new TaskPriorityEvaluationDto(id, 95, new ObjectMapper().createObjectNode(), Instant.now());
        when(taskPriorityEvaluationService.findForTask(id)).thenReturn(Optional.of(evaluation));
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
        assertThatThrownBy(() -> new JsonMapper().readValue("{\"content\":\"Only title\"}", TaskUpdateRequest.class))
                .isInstanceOf(MismatchedInputException.class);
    }
}
