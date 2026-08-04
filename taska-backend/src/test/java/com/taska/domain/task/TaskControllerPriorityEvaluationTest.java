package com.taska.domain.task;

import com.taska.domain.priority.TaskPriorityEvaluationDto;
import com.taska.domain.priority.TaskPriorityEvaluationService;
import com.taska.domain.notification.TaskChangePublisher;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TaskControllerPriorityEvaluationTest {
    private final TaskService taskService = mock(TaskService.class);
    private final TaskMapper taskMapper = mock(TaskMapper.class);
    private final TaskPriorityEvaluationService evaluationService = mock(TaskPriorityEvaluationService.class);
    private final TaskChangePublisher taskChangePublisher = mock(TaskChangePublisher.class);
    private final TaskController controller = new TaskController(taskService, taskMapper, evaluationService, new ObjectMapper(), taskChangePublisher);

    @Test void returnsEvaluationWhenPresent() {
        UUID id = UUID.randomUUID();
        var evaluation = new TaskPriorityEvaluationDto(id, 95, new ObjectMapper().createObjectNode(), Instant.now());
        when(evaluationService.findForTask(id)).thenReturn(Optional.of(evaluation));
        var response = controller.getPriorityEvaluation(id);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(evaluation);
    }

    @Test void returnsNoContentWhenEvaluationIsMissing() {
        UUID id = UUID.randomUUID();
        when(evaluationService.findForTask(id)).thenReturn(Optional.empty());
        var response = controller.getPriorityEvaluation(id);
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThat(response.getBody()).isNull();
    }
}
