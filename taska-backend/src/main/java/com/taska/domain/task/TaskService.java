package com.taska.domain.task;

import com.taska.domain.project.ProjectRepository;
import com.taska.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;

    @Transactional(readOnly = true)
    public List<Task> findAll(UUID projectId, UUID sectionId, String label, String filter, boolean showCompleted) {
        if (filter != null) {
            LocalDate today = LocalDate.now();
            return switch (filter) {
                case "today" -> taskRepo.findByDueDateAndIsCompletedFalseOrderByPositionAsc(today);
                case "overdue" -> taskRepo.findByDueDateBeforeAndIsCompletedFalseOrderByDueDateAsc(today);
                case "upcoming" -> taskRepo.findByDueDateBetweenAndIsCompletedFalseOrderByDueDateAsc(
                        today.plusDays(1), today.plusDays(14));
                default -> taskRepo.findAll();
            };
        }
        if (label != null) {
            return showCompleted ? taskRepo.findByLabel(label) : taskRepo.findByLabelAndIsCompletedFalse(label);
        }
        if (projectId != null && sectionId != null) {
            return showCompleted
                    ? taskRepo.findByProjectIdAndSectionIdOrderByPositionAsc(projectId, sectionId)
                    : taskRepo.findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(projectId, sectionId);
        }
        if (projectId != null) {
            return showCompleted
                    ? taskRepo.findByProjectIdOrderByPositionAsc(projectId)
                    : taskRepo.findByProjectIdAndIsCompletedFalseOrderByPositionAsc(projectId);
        }
        if (sectionId != null) {
            return showCompleted
                    ? taskRepo.findBySectionIdOrderByPositionAsc(sectionId)
                    : taskRepo.findBySectionIdAndIsCompletedFalseOrderByPositionAsc(sectionId);
        }
        return taskRepo.findAll();
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

        return taskRepo.save(t);
    }

    public Task update(UUID id, TaskRequest req) {
        Task t = getOrThrow(id);
        if (req.content() != null) t.setContent(req.content());
        if (req.description() != null) t.setDescription(req.description());
        if (req.projectId() != null) t.setProjectId(req.projectId());
        if (req.sectionId() != null) t.setSectionId(req.sectionId());
        if (req.parentId() != null) t.setParentId(req.parentId());
        if (req.order() != null) t.setPosition(req.order());
        if (req.priority() != null) t.setPriority(req.priority());
        if (req.labels() != null) t.setLabels(req.labels());
        if (req.dueDate() != null) t.setDueDate(req.dueDate());
        if (req.dueDateTime() != null) t.setDueDateTime(req.dueDateTime());
        if (req.isRecurring() != null) t.setIsRecurring(req.isRecurring());
        if (req.estimateMinutes() != null) t.setEstimateMinutes(req.estimateMinutes());
        if (req.mentionContext() != null) t.setMentionContext(req.mentionContext());
        if (req.recurrenceRule() != null) t.setRecurrenceRule(req.recurrenceRule());
        return taskRepo.save(t);
    }

    public void delete(UUID id) {
        taskRepo.delete(getOrThrow(id));
    }

    public Task close(UUID id) {
        Task t = getOrThrow(id);
        t.setIsCompleted(true);
        t.setCompletedAt(Instant.now());
        return taskRepo.save(t);
    }

    public Task reopen(UUID id) {
        Task t = getOrThrow(id);
        t.setIsCompleted(false);
        t.setCompletedAt(null);
        return taskRepo.save(t);
    }

    @Transactional(readOnly = true)
    public List<Task> getSubtasks(UUID parentId) {
        return taskRepo.findByParentIdOrderByPositionAsc(parentId);
    }

    Task getOrThrow(UUID id) {
        return taskRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }
}
