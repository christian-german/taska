package com.taska.domain.priority.controller;

import com.taska.domain.priority.TaskPriorityEvaluation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskPriorityEvaluationMapper {
  default TaskPriorityEvaluationDto toDto(TaskPriorityEvaluation taskPriorityEvaluation) {
    return TaskPriorityEvaluationDto.from(taskPriorityEvaluation);
  }
}
