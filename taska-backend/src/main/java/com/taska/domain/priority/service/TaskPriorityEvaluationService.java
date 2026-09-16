package com.taska.domain.priority.service;

import com.taska.domain.priority.repository.TaskPriorityEvaluation;
import com.taska.domain.priority.repository.TaskPriorityEvaluationRepository;
import com.taska.domain.task.repository.Task;
import com.taska.domain.task.TaskType;
import com.taska.domain.task.repository.TaskRepository;
import com.taska.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskPriorityEvaluationService {

  private final TaskRepository taskRepository;
  private final TaskPriorityEvaluationRepository taskPriorityEvaluationRepository;
  private final OpenAiPriorityAssessmentClient priorityAssessmentClient;
  private final PriorityAssessmentValidator priorityAssessmentValidator;
  private final TaskPriorityScorer taskPriorityScorer;
  private final JsonMapper jsonMapper;

  public Optional<TaskPriorityEvaluation> findForTask(UUID taskId) {
    if (!taskRepository.existsById(taskId)) {
      throw new ResourceNotFoundException("Task not found: " + taskId);
    }
    return taskPriorityEvaluationRepository.findByTaskId(taskId);
  }

  @Transactional
  public void evaluate(List<Task> tasks) {
    if (tasks.isEmpty()) {
      return;
    }
    Map<UUID, Instant> taskVersions =
        tasks.stream().collect(Collectors.toMap(Task::getId, Task::getUpdatedAt));
    Set<UUID> taskIds = taskVersions.keySet();
    PriorityEvaluationBatchResponse evaluationResponse =
        priorityAssessmentClient.assess(PriorityEvaluationBatchRequest.from(tasks));
    priorityAssessmentValidator.validateEnvelope(evaluationResponse, taskIds);
    for (var assessment : evaluationResponse.evaluations()) {
      if (!priorityAssessmentValidator.isValid(assessment)) {
        continue;
      }
      Task current = taskRepository.findById(assessment.taskId()).orElse(null);
      if (!eligibleAndUnchanged(current, taskVersions.get(assessment.taskId()))) {
        continue;
      }
      TaskPriorityEvaluation evaluation = new TaskPriorityEvaluation();
      evaluation.setTaskId(current.getId());
      evaluation.setScore(
          taskPriorityScorer.total(
              assessment.urgency(),
              assessment.impact(),
              assessment.risk(),
              assessment.durationMinutes()));
      evaluation.setComponents(components(assessment));
      evaluation.setComputedAt(Instant.now());
      taskPriorityEvaluationRepository.save(evaluation);
    }
  }

  private boolean eligibleAndUnchanged(Task task, Instant version) {
    return task != null
        && !Boolean.TRUE.equals(task.getIsCompleted())
        && !Boolean.TRUE.equals(task.getIsRecurring())
        && task.getType() == TaskType.TODO
        && java.util.Objects.equals(task.getUpdatedAt(), version);
  }

  private JsonNode components(PriorityEvaluationBatchResponse.Assessment assessment) {
    ObjectNode root = jsonMapper.createObjectNode();
    component(
        root,
        "urgency",
        assessment.urgency().name(),
        assessment.urgencyConfidence(),
        assessment.urgencyReason(),
        taskPriorityScorer.urgencyPoints(assessment.urgency()));
    component(
        root,
        "impact",
        assessment.impact().name(),
        assessment.impactConfidence(),
        assessment.impactReason(),
        taskPriorityScorer.impactPoints(assessment.impact()));
    component(
        root,
        "risk",
        assessment.risk().name(),
        assessment.riskConfidence(),
        assessment.riskReason(),
        taskPriorityScorer.riskPoints(assessment.risk()));
    component(
        root,
        "duration",
        assessment.durationMinutes(),
        assessment.durationConfidence(),
        assessment.durationReason(),
        taskPriorityScorer.durationPoints(assessment.durationMinutes()));
    return root;
  }

  private void component(
      ObjectNode root, String name, Object value, Double confidence, String reason, int points) {
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
