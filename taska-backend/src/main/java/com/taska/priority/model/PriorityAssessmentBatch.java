package com.taska.priority.model;

import com.taska.task.model.Task;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PriorityAssessmentBatch(List<TaskInput> tasks) {
    public static PriorityAssessmentBatch from(List<Task> tasks) {
        return new PriorityAssessmentBatch(tasks.stream().map(TaskInput::from).toList());
    }

    public record TaskInput(UUID taskId, String content, String description, Instant scheduledAt, Instant createdAt) {
        static TaskInput from(Task task) {
            return new TaskInput(task.getId(), task.getContent(), task.getDescription(), task.getScheduledAt(), task.getCreatedAt());
        }
    }
}
