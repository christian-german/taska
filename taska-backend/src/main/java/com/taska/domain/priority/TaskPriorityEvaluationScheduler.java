package com.taska.domain.priority;

import com.taska.domain.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskPriorityEvaluationScheduler {
    private final TaskRepository taskRepository;
    private final TaskPriorityEvaluationService evaluationService;
    @Value("${taska.priority-evaluation.batch-size:10}") private int batchSize;

    @Scheduled(fixedDelayString = "${taska.priority-evaluation.scheduler-delay:60000}")
    public void evaluateMissingTasks() {
        try {
            evaluationService.evaluate(taskRepository.findEligibleTasksWithoutPriorityEvaluation(PageRequest.of(0, Math.min(batchSize, 10))));
        } catch (Exception exception) {
            log.warn("Priority evaluation batch failed; it will be retried", exception);
        }
    }
}
