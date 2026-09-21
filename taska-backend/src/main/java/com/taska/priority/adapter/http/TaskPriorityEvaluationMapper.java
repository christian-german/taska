package com.taska.priority.adapter.http;

import com.taska.platform.config.ApiMapperConfig;
import com.taska.priority.model.TaskPriorityEvaluation;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface TaskPriorityEvaluationMapper {

    TaskPriorityEvaluationDto toDto(TaskPriorityEvaluation taskPriorityEvaluation);
}
