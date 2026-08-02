package com.taska.domain.priority;

import com.taska.domain.task.Task;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PriorityEvaluationBatchRequest(List<TaskInput> tasks) {
    public static PriorityEvaluationBatchRequest from(List<Task> tasks) {
        return new PriorityEvaluationBatchRequest(tasks.stream().map(TaskInput::from).toList());
    }

    public record TaskInput(UUID taskId, String content, String description, Instant scheduledAt, Instant createdAt) {
        static TaskInput from(Task task) {
            return new TaskInput(task.getId(), task.getContent(), task.getDescription(), task.getScheduledAt(), task.getCreatedAt());
        }
    }
}
