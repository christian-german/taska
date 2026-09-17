package com.taska.android.data.model

enum class TaskRepresentationKind {
  NON_RECURRING,
  RECURRING_SERIES,
  RECURRING_OCCURRENCE,
}

sealed interface TaskDto {
  val kind: TaskRepresentationKind
  val id: String
  val content: String
  val type: String?
  val description: String?
  val projectId: String?
  val parentId: String?
  val order: Int?
  val priority: Int?
  val labels: List<String>?
  val scheduledAt: String?
  val dueAt: String?
  val allDay: Boolean
  val estimateMinutes: Int?
  val mentionContext: String?
  val createdAt: String?
  val updatedAt: String?

  val isCompleted: Boolean?
    get() = null

  val completedAt: String?
    get() = null

  val isRecurring: Boolean
    get() = kind != TaskRepresentationKind.NON_RECURRING

  val recurrenceRule: String?
    get() = null

  val instanceId: String?
    get() = null

  val occurrenceScheduledAt: String?
    get() = null

  val isVirtual: Boolean?
    get() = null

  val rruleEndsAt: String?
    get() = null
}

data class NonRecurringTaskDto(
  override val kind: TaskRepresentationKind = TaskRepresentationKind.NON_RECURRING,
  override val id: String,
  override val content: String,
  override val type: String? = "TODO",
  override val description: String?,
  override val projectId: String?,
  override val parentId: String?,
  override val order: Int?,
  override val priority: Int?,
  override val labels: List<String>?,
  override val scheduledAt: String?,
  override val dueAt: String? = null,
  override val allDay: Boolean = false,
  override val estimateMinutes: Int?,
  override val mentionContext: String? = null,
  override val createdAt: String?,
  override val updatedAt: String?,
  override val isCompleted: Boolean?,
  override val completedAt: String?,
) : TaskDto

data class RecurringTaskSeriesDto(
  override val kind: TaskRepresentationKind = TaskRepresentationKind.RECURRING_SERIES,
  override val id: String,
  override val content: String,
  override val type: String? = "TODO",
  override val description: String?,
  override val projectId: String?,
  override val parentId: String?,
  override val order: Int?,
  override val priority: Int?,
  override val labels: List<String>?,
  override val scheduledAt: String?,
  override val dueAt: String? = null,
  override val allDay: Boolean = false,
  override val estimateMinutes: Int?,
  override val mentionContext: String? = null,
  override val createdAt: String?,
  override val updatedAt: String?,
  override val recurrenceRule: String? = null,
  override val rruleEndsAt: String? = null,
) : TaskDto

data class RecurringTaskOccurrenceDto(
  override val kind: TaskRepresentationKind = TaskRepresentationKind.RECURRING_OCCURRENCE,
  override val id: String,
  override val content: String,
  override val type: String? = "TODO",
  override val description: String?,
  override val projectId: String?,
  override val parentId: String?,
  override val order: Int?,
  override val priority: Int?,
  override val labels: List<String>?,
  override val scheduledAt: String?,
  override val dueAt: String? = null,
  override val allDay: Boolean = false,
  override val estimateMinutes: Int?,
  override val mentionContext: String? = null,
  override val createdAt: String?,
  override val updatedAt: String?,
  override val recurrenceRule: String? = null,
  override val rruleEndsAt: String? = null,
  override val isCompleted: Boolean?,
  override val completedAt: String?,
  override val instanceId: String?,
  override val occurrenceScheduledAt: String,
  override val isVirtual: Boolean,
) : TaskDto

@Suppress("FunctionName")
fun TaskDto(
  id: String,
  content: String,
  type: String? = "TODO",
  description: String?,
  projectId: String?,
  parentId: String?,
  order: Int?,
  priority: Int?,
  labels: List<String>?,
  isCompleted: Boolean?,
  scheduledAt: String?,
  dueAt: String? = null,
  allDay: Boolean = false,
  isRecurring: Boolean?,
  recurrenceRule: String? = null,
  estimateMinutes: Int?,
  mentionContext: String? = null,
  createdAt: String?,
  updatedAt: String?,
  completedAt: String?,
  instanceId: String? = null,
  occurrenceScheduledAt: String? = null,
  isVirtual: Boolean? = null,
  rruleEndsAt: String? = null,
): TaskDto {
  if (isRecurring != true) {
    return NonRecurringTaskDto(
      id = id,
      content = content,
      type = type,
      description = description,
      projectId = projectId,
      parentId = parentId,
      order = order,
      priority = priority,
      labels = labels,
      scheduledAt = scheduledAt,
      dueAt = dueAt,
      allDay = allDay,
      estimateMinutes = estimateMinutes,
      mentionContext = mentionContext,
      createdAt = createdAt,
      updatedAt = updatedAt,
      isCompleted = isCompleted,
      completedAt = completedAt,
    )
  }
  if (occurrenceScheduledAt == null) {
    return RecurringTaskSeriesDto(
      id = id,
      content = content,
      type = type,
      description = description,
      projectId = projectId,
      parentId = parentId,
      order = order,
      priority = priority,
      labels = labels,
      scheduledAt = scheduledAt,
      dueAt = dueAt,
      allDay = allDay,
      estimateMinutes = estimateMinutes,
      mentionContext = mentionContext,
      createdAt = createdAt,
      updatedAt = updatedAt,
      recurrenceRule = recurrenceRule,
      rruleEndsAt = rruleEndsAt,
    )
  }
  return RecurringTaskOccurrenceDto(
    id = id,
    content = content,
    type = type,
    description = description,
    projectId = projectId,
    parentId = parentId,
    order = order,
    priority = priority,
    labels = labels,
    scheduledAt = scheduledAt,
    dueAt = dueAt,
    allDay = allDay,
    estimateMinutes = estimateMinutes,
    mentionContext = mentionContext,
    createdAt = createdAt,
    updatedAt = updatedAt,
    recurrenceRule = recurrenceRule,
    rruleEndsAt = rruleEndsAt,
    isCompleted = isCompleted,
    completedAt = completedAt,
    instanceId = instanceId,
    occurrenceScheduledAt = occurrenceScheduledAt,
    isVirtual = isVirtual ?: (instanceId == null),
  )
}

fun TaskDto.copy(
  id: String = this.id,
  content: String = this.content,
  type: String? = this.type,
  description: String? = this.description,
  projectId: String? = this.projectId,
  parentId: String? = this.parentId,
  order: Int? = this.order,
  priority: Int? = this.priority,
  labels: List<String>? = this.labels,
  isCompleted: Boolean? = this.isCompleted,
  scheduledAt: String? = this.scheduledAt,
  dueAt: String? = this.dueAt,
  allDay: Boolean = this.allDay,
  isRecurring: Boolean? = this.isRecurring,
  recurrenceRule: String? = this.recurrenceRule,
  estimateMinutes: Int? = this.estimateMinutes,
  mentionContext: String? = this.mentionContext,
  createdAt: String? = this.createdAt,
  updatedAt: String? = this.updatedAt,
  completedAt: String? = this.completedAt,
  instanceId: String? = this.instanceId,
  occurrenceScheduledAt: String? = this.occurrenceScheduledAt,
  isVirtual: Boolean? = this.isVirtual,
  rruleEndsAt: String? = this.rruleEndsAt,
) =
  TaskDto(
    id,
    content,
    type,
    description,
    projectId,
    parentId,
    order,
    priority,
    labels,
    isCompleted,
    scheduledAt,
    dueAt,
    allDay,
    isRecurring,
    recurrenceRule,
    estimateMinutes,
    mentionContext,
    createdAt,
    updatedAt,
    completedAt,
    instanceId,
    occurrenceScheduledAt,
    isVirtual,
    rruleEndsAt,
  )
