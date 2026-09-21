package com.taska.priority.adapter.http;

import com.taska.priority.application.TaskPriorityEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the computed priority evaluation of one task.
 *
 * <p>
 * The resource is addressed under {@code /tasks} because that is what it describes, but the evaluation, its DTO and its mapper all belong to the
 * priority module. Serving it from here is what keeps the task module from depending on priority.
 */
@RestController
@RequestMapping("/tasks/{taskId}/priority-evaluation")
@RequiredArgsConstructor
public class TaskPriorityEvaluationController {

    private final TaskPriorityEvaluationService priorityEvaluationService;
    private final TaskPriorityEvaluationMapper priorityEvaluationMapper;

    /**
     * Returns the stored priority evaluation of a task, or 204 when none was computed.
     *
     * @param taskId the task UUID
     * @return the evaluation DTO, or an empty 204 response
     */
    @GetMapping
    public ResponseEntity<TaskPriorityEvaluationDto> getPriorityEvaluation(@PathVariable UUID taskId) {
        return priorityEvaluationService.findForTask(taskId)
                .map(priorityEvaluationMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
