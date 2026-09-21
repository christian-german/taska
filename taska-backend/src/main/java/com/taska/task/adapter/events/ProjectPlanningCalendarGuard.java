package com.taska.task.adapter.events;

import com.taska.planningcalendar.application.PlanningCalendarService;
import com.taska.project.model.ProjectPlanningCalendarChangeRequested;
import com.taska.task.model.Task;
import com.taska.task.persistence.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Rejects a project's move to a planning calendar that one of its scheduled tasks would violate.
 *
 * <p>
 * "A scheduled task must fit its project's planning calendar" is a task-module invariant: it is enforced on every task write by the definition
 * service. Changing the project's calendar is the other half of the same invariant, so it is enforced here rather than in the project module, which
 * owns the assignment but not the rule. The listener is synchronous, so throwing rolls back the project update.
 */
@Component
@RequiredArgsConstructor
public class ProjectPlanningCalendarGuard {

    private final TaskRepository taskRepository;
    private final PlanningCalendarService planningCalendarService;

    /**
     * Vetoes the requested calendar when an open scheduled task would fall outside it.
     *
     * @param event project and planning calendar the change would apply
     * @throws IllegalArgumentException when an existing scheduled task would become invalid
     */
    @EventListener
    public void onPlanningCalendarChangeRequested(ProjectPlanningCalendarChangeRequested event) {
        boolean hasIncompatibleTask = taskRepository.findByProjectIdAndScheduledAtIsNotNullAndIsCompletedFalseOrderByScheduledAtAsc(event.projectId())
                .stream()
                .anyMatch(task -> outsideCalendar(task, event.planningCalendarId()));
        if (hasIncompatibleTask) {
            throw new IllegalArgumentException("Planning calendar does not allow an existing scheduled task");
        }
    }

    private boolean outsideCalendar(Task task, java.util.UUID planningCalendarId) {
        return !planningCalendarService.allows(planningCalendarId, task.getScheduledAt(), task.isAllDay());
    }
}
