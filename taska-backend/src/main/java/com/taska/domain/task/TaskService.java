package com.taska.domain.task;

import com.taska.domain.project.ProjectRepository;
import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<Task> findAll(UUID projectId, UUID sectionId, String label, String filter, boolean showCompleted) {
        if (filter != null) {
            Instant startOfToday    = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant startOfTomorrow = LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            return switch (filter) {
                case "today"    -> taskRepository.findByDueAtBetweenAndIsCompletedFalseOrderByDueAtAsc(startOfToday, startOfTomorrow);
                case "overdue"  -> taskRepository.findByDueAtBeforeAndIsCompletedFalseOrderByDueAtAsc(startOfToday);
                case "upcoming" -> taskRepository.findByDueAtBetweenAndIsCompletedFalseOrderByDueAtAsc(
                        startOfTomorrow, LocalDate.now(ZoneOffset.UTC).plusDays(14).atStartOfDay(ZoneOffset.UTC).toInstant());
                default -> taskRepository.findAll();
            };
        }
        if (label != null) {
            return showCompleted ? taskRepository.findByLabel(label) : taskRepository.findByLabelAndIsCompletedFalse(label);
        }
        if (projectId != null && sectionId != null) {
            return showCompleted
                    ? taskRepository.findByProjectIdAndSectionIdOrderByPositionAsc(projectId, sectionId)
                    : taskRepository.findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(projectId, sectionId);
        }
        if (projectId != null) {
            return showCompleted
                    ? taskRepository.findByProjectIdOrderByPositionAsc(projectId)
                    : taskRepository.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
        }
        if (sectionId != null) {
            return showCompleted
                    ? taskRepository.findBySectionIdOrderByPositionAsc(sectionId)
                    : taskRepository.findBySectionIdAndIsCompletedFalseOrderByPositionAsc(sectionId);
        }
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Task findById(UUID id) {
        return getOrThrow(id);
    }

    public Task create(TaskRequest req) {
        Task t = new Task();
        t.setContent(req.content());
        t.setDescription(req.description());
        t.setSectionId(req.sectionId());
        t.setParentId(req.parentId());
        t.setPosition(req.order() != null ? req.order() : 0);
        t.setPriority(req.priority() != null ? req.priority() : 1);
        t.setLabels(req.labels() != null ? req.labels() : new ArrayList<>());
        t.setDueAt(req.dueAt());
        t.setAllDay(req.allDay() != null ? req.allDay() : false);
        t.setIsRecurring(req.isRecurring() != null ? req.isRecurring() : false);
        t.setEstimateMinutes(req.estimateMinutes());
        t.setMentionContext(req.mentionContext());
        t.setRecurrenceRule(req.recurrenceRule());

        UUID projectId = req.projectId();
        if (projectId == null && req.parentId() == null) {
            projectId = projectRepository.findByIsInboxProjectTrue()
                    .orElseThrow(() -> new ResourceNotFoundException("Inbox project not found"))
                    .getId();
        }
        t.setProjectId(projectId);

        return taskRepository.save(t);
    }

    public Task update(UUID id, TaskRequest taskRequest) {
        Task task = getOrThrow(id);
        if (taskRequest.content() != null) task.setContent(taskRequest.content());
        if (taskRequest.description() != null) task.setDescription(taskRequest.description());
        if (taskRequest.projectId() != null) task.setProjectId(taskRequest.projectId());
        if (taskRequest.sectionId() != null) task.setSectionId(taskRequest.sectionId());
        if (taskRequest.parentId() != null) task.setParentId(taskRequest.parentId());
        if (taskRequest.order() != null) task.setPosition(taskRequest.order());
        if (taskRequest.priority() != null) task.setPriority(taskRequest.priority());
        if (taskRequest.labels() != null) task.setLabels(taskRequest.labels());
        if (taskRequest.dueAt() != null) task.setDueAt(taskRequest.dueAt());
        if (taskRequest.allDay() != null) task.setAllDay(taskRequest.allDay());
        if (taskRequest.isRecurring() != null) task.setIsRecurring(taskRequest.isRecurring());
        if (taskRequest.estimateMinutes() != null) task.setEstimateMinutes(taskRequest.estimateMinutes());
        if (taskRequest.mentionContext() != null) task.setMentionContext(taskRequest.mentionContext());
        if (taskRequest.recurrenceRule() != null) task.setRecurrenceRule(taskRequest.recurrenceRule());
        return taskRepository.save(task);
    }

    public void delete(UUID taskId) {
        taskRepository.delete(getOrThrow(taskId));
    }

    public Task close(UUID taskId) {
        Task task = getOrThrow(taskId);
        task.setIsCompleted(true);
        task.setCompletedAt(Instant.now());
        return taskRepository.save(task);
    }

    public Task reopen(UUID taskId) {
        Task task = getOrThrow(taskId);
        task.setIsCompleted(false);
        task.setCompletedAt(null);
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getSubtasks(UUID parentTaskId) {
        return taskRepository.findByParentIdOrderByPositionAsc(parentTaskId);
    }

    public Task getOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    public List<Task> findTasksDueAround(Instant in15min) {
        return taskRepository.findTasksDueAround(in15min);
    }
}
