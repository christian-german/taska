package com.taska.priority.adapter.events;

import com.taska.priority.persistence.TaskPriorityEvaluationRepository;
import com.taska.task.model.TaskMutatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Inbound adapter discarding a task's computed priority once the task itself changed.
 *
 * <p>
 * An evaluation describes a task's content at the moment it was scored. The listener is synchronous and runs inside the publishing transaction, so
 * the stale row is gone before the mutation commits and before a task deletion reaches its foreign key.
 */
@Component
@RequiredArgsConstructor
public class TaskMutationListener {

    private final TaskPriorityEvaluationRepository taskPriorityEvaluationRepository;

    /**
     * Purges the stored evaluation of a mutated task.
     *
     * @param event mutated task identity
     */
    @EventListener
    public void onTaskMutated(TaskMutatedEvent event) {
        taskPriorityEvaluationRepository.deleteByTaskId(event.taskId());
    }
}
