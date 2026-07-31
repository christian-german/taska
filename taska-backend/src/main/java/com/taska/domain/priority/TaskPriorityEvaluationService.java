package com.taska.domain.priority;

import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import com.taska.domain.task.TaskType;
import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskPriorityEvaluationService {

    private final TaskRepository taskRepository;
    private final TaskPriorityEvaluationRepository evaluationRepository;
    private final OpenAiPriorityAssessmentClient assessmentClient;
    private final PriorityAssessmentValidator validator;
    private final TaskPriorityScorer scorer;
    private final JsonMapper jsonMapper;

    public Optional<TaskPriorityEvaluationDto> findForTask(UUID taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task not found: " + taskId);
        }
        return evaluationRepository.findByTaskId(taskId).map(TaskPriorityEvaluationDto::from);
    }

    @Transactional
    public void evaluate(List<Task> tasks) {
        if (tasks.isEmpty()) return;
        Map<UUID, Instant> versions = tasks.stream().collect(Collectors.toMap(Task::getId, Task::getUpdatedAt));
        Set<UUID> ids = versions.keySet();
        PriorityEvaluationBatchResponse response = assessmentClient.assess(PriorityEvaluationBatchRequest.from(tasks));
        validator.validateEnvelope(response, ids);
        for (var assessment : response.evaluations()) {
            if (!validator.isValid(assessment)) continue;
            Task current = taskRepository.findById(assessment.taskId()).orElse(null);
            if (!eligibleAndUnchanged(current, versions.get(assessment.taskId()))) continue;
            TaskPriorityEvaluation evaluation = new TaskPriorityEvaluation();
            evaluation.setTaskId(current.getId());
            evaluation.setScore(scorer.total(assessment.urgency(), assessment.impact(), assessment.risk(), assessment.durationMinutes()));
            evaluation.setComponents(components(assessment));
            evaluation.setComputedAt(Instant.now());
            evaluationRepository.save(evaluation);
        }
    }

    private boolean eligibleAndUnchanged(Task task, Instant version) {
        return task != null && !Boolean.TRUE.equals(task.getIsCompleted()) && !Boolean.TRUE.equals(task.getIsRecurring())
                && task.getType() == TaskType.TODO && java.util.Objects.equals(task.getUpdatedAt(), version);
    }

    private JsonNode components(PriorityEvaluationBatchResponse.Assessment a) {
        ObjectNode root = jsonMapper.createObjectNode();
        component(root, "urgency", a.urgency().name(), a.urgencyConfidence(), a.urgencyReason(), scorer.urgencyPoints(a.urgency()));
        component(root, "impact", a.impact().name(), a.impactConfidence(), a.impactReason(), scorer.impactPoints(a.impact()));
        component(root, "risk", a.risk().name(), a.riskConfidence(), a.riskReason(), scorer.riskPoints(a.risk()));
        component(root, "duration", a.durationMinutes(), a.durationConfidence(), a.durationReason(), scorer.durationPoints(a.durationMinutes()));
        return root;
    }

    private void component(ObjectNode root, String name, Object value, Double confidence, String reason, int points) {
        ObjectNode component = root.putObject(name);
        if (value instanceof String string) {
            component.put("value", string);
        } else {
            component.put("value", (Integer) value);
        }
        component.put("source", "LLM");
        component.put("confidence", confidence);
        component.put("reason", reason);
        component.put("points", points);
    }
}
