package com.taska.domain.priority.controller;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.priority.repository.TaskPriorityEvaluation;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface TaskPriorityEvaluationMapper {

  TaskPriorityEvaluationDto toDto(TaskPriorityEvaluation taskPriorityEvaluation);
}
