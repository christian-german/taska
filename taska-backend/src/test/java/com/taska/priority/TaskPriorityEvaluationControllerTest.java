package com.taska.priority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.taska.priority.adapter.http.TaskPriorityEvaluationController;
import com.taska.priority.adapter.http.TaskPriorityEvaluationDto;
import com.taska.priority.adapter.http.TaskPriorityEvaluationMapper;
import com.taska.priority.application.TaskPriorityEvaluationService;
import com.taska.priority.model.TaskPriorityEvaluation;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class TaskPriorityEvaluationControllerTest {

    private final TaskPriorityEvaluationService taskPriorityEvaluationService = mock(TaskPriorityEvaluationService.class);
    private final TaskPriorityEvaluationMapper priorityEvaluationMapper = mock(TaskPriorityEvaluationMapper.class);
    private final TaskPriorityEvaluationController controller = new TaskPriorityEvaluationController(
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
        var evaluation = new TaskPriorityEvaluationDto(entity.getTaskId(), entity.getScore(), entity.getComponents(), entity.getComputedAt());
        when(taskPriorityEvaluationService.findForTask(id)).thenReturn(Optional.of(entity));
        when(priorityEvaluationMapper.toDto(entity)).thenReturn(evaluation);

        var responseEntity = controller.getPriorityEvaluation(id);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isEqualTo(evaluation);
    }

    @Test
    void returnsNoContentWhenEvaluationIsMissing() {
        UUID id = UUID.randomUUID();
        when(taskPriorityEvaluationService.findForTask(id)).thenReturn(Optional.empty());

        var responseEntity = controller.getPriorityEvaluation(id);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(204);
        assertThat(responseEntity.getBody()).isNull();
    }
}
