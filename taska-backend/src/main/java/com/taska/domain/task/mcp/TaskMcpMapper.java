package com.taska.domain.task.mcp;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.task.service.TaskCreateParameters;
import com.taska.domain.task.service.TaskPatchParameters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps MCP tool inputs to the task service contract. */
@Mapper(config = ApiMapperConfig.class)
public interface TaskMcpMapper {

  /** MCP creates plain todos; the tool contract exposes no task type. */
  @Mapping(target = "position", source = "order", defaultValue = "0")
  @Mapping(target = "allDay", defaultValue = "false")
  @Mapping(target = "recurring", source = "isRecurring", defaultValue = "false")
  @Mapping(target = "type", constant = "TODO")
  TaskCreateParameters toParameters(TaskMcpTools.TaskCreateInput taskCreateInput);

  /** {@code clearPriority} drives the separate priorityProvided flag, not a patched property. */
  @Mapping(target = "position", source = "order")
  @Mapping(target = "recurring", source = "isRecurring")
  @Mapping(target = "type", ignore = true)
  TaskPatchParameters toParameters(TaskMcpTools.TaskUpdateInput taskUpdateInput);
}
