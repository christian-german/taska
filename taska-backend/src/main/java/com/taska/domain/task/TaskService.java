package com.taska.domain.task;

import com.taska.domain.project.ProjectRepository;
import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepo;

    @Transactional(readOnly = true)
    public List<Task> findAll(UUID projectId, UUID sectionId, String label, String filter, boolean showCompleted) {
        if (filter != null) {
            LocalDate today = LocalDate.now();
            return switch (filter) {
                case "today" -> taskRepository.findByDueDateAndIsCompletedFalseOrderByPositionAsc(today);
                case "overdue" -> taskRepository.findByDueDateBeforeAndIsCompletedFalseOrderByDueDateAsc(today);
                case "upcoming" -> taskRepository.findByDueDateBetweenAndIsCompletedFalseOrderByDueDateAsc(
                        today.plusDays(1), today.plusDays(14));
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
        t.setDueDate(req.dueDate());
        t.setDueDateTime(req.dueDateTime());
        t.setIsRecurring(req.isRecurring() != null ? req.isRecurring() : false);
        t.setEstimateMinutes(req.estimateMinutes());
        t.setMentionContext(req.mentionContext());
        t.setRecurrenceRule(req.recurrenceRule());

        UUID projectId = req.projectId();
        if (projectId == null && req.parentId() == null) {
            projectId = projectRepo.findByIsInboxProjectTrue()
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
        if (taskRequest.dueDate() != null) task.setDueDate(taskRequest.dueDate());
        if (taskRequest.dueDateTime() != null) task.setDueDateTime(taskRequest.dueDateTime());
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

    Task getOrThrow(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
    }

    public List<Task> findTasksDueAround(LocalDateTime in15min) {
        return taskRepository.findTasksDueAround(in15min);
    }
}
