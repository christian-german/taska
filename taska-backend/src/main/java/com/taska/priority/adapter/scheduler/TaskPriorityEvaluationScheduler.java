package com.taska.priority.adapter.scheduler;

import com.taska.priority.application.TaskPriorityEvaluationService;
import com.taska.task.persistence.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskPriorityEvaluationScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskPriorityEvaluationScheduler.class);

    private final TaskRepository taskRepository;
    private final TaskPriorityEvaluationService taskPriorityEvaluationService;
    private final int batchSize;

    public TaskPriorityEvaluationScheduler(
            TaskRepository taskRepository,
            TaskPriorityEvaluationService taskPriorityEvaluationService,
            @Value("${taska.priority-evaluation.batch-size:10}") int batchSize) {
        this.taskRepository = taskRepository;
        this.taskPriorityEvaluationService = taskPriorityEvaluationService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${taska.priority-evaluation.scheduler-delay:60000}")
    public void evaluateMissingTasks() {
        try {
            taskPriorityEvaluationService
                    .evaluate(taskRepository.findEligibleTasksWithoutPriorityEvaluation(PageRequest.of(0, Math.min(batchSize, 10))));
        } catch (Exception exception) {
            log.warn("Priority evaluation batch failed; it will be retried", exception);
        }
    }
}
