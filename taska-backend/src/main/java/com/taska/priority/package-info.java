/**
 * Computed priority evaluation of tasks, assessed in batches by a language model.
 *
 * <p>
 * Reads the task model to score it and discards a stored evaluation when {@link com.taska.task.model.TaskMutatedEvent} says the scored content
 * changed. The task module never calls into this one.
 */
@org.springframework.modulith.ApplicationModule(displayName = "Priority", allowedDependencies = {"platform :: config", "platform :: exception",
        "task :: model", "task :: persistence"})
package com.taska.priority;
