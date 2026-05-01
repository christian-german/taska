package com.taska.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByProjectIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    List<Task> findBySectionIdAndIsCompletedFalseOrderByPositionAsc(UUID sectionId);

    List<Task> findByProjectIdAndSectionIdAndIsCompletedFalseOrderByPositionAsc(UUID projectId, UUID sectionId);

    List<Task> findByProjectIdAndSectionIdIsNullAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    List<Task> findByDueDateAndIsCompletedFalseOrderByPositionAsc(LocalDate dueDate);

    List<Task> findByDueDateBeforeAndIsCompletedFalseOrderByDueDateAsc(LocalDate date);

    List<Task> findByDueDateBetweenAndIsCompletedFalseOrderByDueDateAsc(LocalDate from, LocalDate to);

    List<Task> findByProjectIdOrderByPositionAsc(UUID projectId);

    List<Task> findBySectionIdOrderByPositionAsc(UUID sectionId);

    List<Task> findByProjectIdAndSectionIdOrderByPositionAsc(UUID projectId, UUID sectionId);

    List<Task> findByProjectIdAndSectionIdIsNullOrderByPositionAsc(UUID projectId);

    List<Task> findByParentIdOrderByPositionAsc(UUID parentId);

    void deleteByProjectId(UUID projectId);

    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label AND t.isCompleted = false ORDER BY t.position ASC")
    List<Task> findByLabelAndIsCompletedFalse(@Param("label") String label);

    @Query("SELECT t FROM Task t JOIN t.labels l WHERE l = :label ORDER BY t.position ASC")
    List<Task> findByLabel(@Param("label") String label);

    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.dueDate IS NOT NULL ORDER BY t.dueDate ASC")
    List<Task> findAllWithDueDateNotCompleted();

    @Query("SELECT t FROM Task t WHERE t.isCompleted = false AND t.dueDate IS NULL ORDER BY t.position ASC")
    List<Task> findAllWithNoDueDateNotCompleted();

    List<Task> findByProjectIdAndDueDateIsNotNullAndIsCompletedFalseOrderByDueDateAsc(UUID projectId);

    List<Task> findByProjectIdAndDueDateIsNullAndIsCompletedFalseOrderByPositionAsc(UUID projectId);

    long countByIsCompletedTrue();

    long countByIsCompletedFalse();

    long countByIsCompletedFalseAndDueDateBefore(LocalDate date);

    List<Task> findByCompletedAtAfterOrderByCompletedAtAsc(Instant since);

    List<Task> findByIsCompletedTrueOrderByCompletedAtDesc();
}
