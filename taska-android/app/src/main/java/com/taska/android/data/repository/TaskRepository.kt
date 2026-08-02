package com.taska.android.data.repository

import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.api.TaskaApi
import com.taska.android.data.model.CloseReopenRequest
import com.taska.android.data.model.DeleteTaskBody
import com.taska.android.data.model.RecurrenceScope
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest

class TaskRepository(private val api: TaskaApi) {

    constructor() : this(RetrofitClient.api)

    suspend fun getTasks(
        projectId: String? = null,
        showCompleted: Boolean = false,
        date: String? = null,
        from: String? = null,
        to: String? = null,
    ): List<TaskDto> = api.getTasks(projectId, showCompleted, date, from, to)

    suspend fun getTask(id: String): TaskDto = api.getTask(id)

    suspend fun getSubtasks(taskId: String): List<TaskDto> = api.getSubtasks(taskId)

    suspend fun createTask(request: TaskRequest): TaskDto = api.createTask(request)

    suspend fun updateTask(id: String, request: TaskRequest): TaskDto = api.updateTask(id, request)

    suspend fun closeTask(taskId: String, occurrenceScheduledAt: String? = null): TaskDto =
        api.closeTask(taskId, CloseReopenRequest(occurrenceScheduledAt))

    suspend fun reopenTask(taskId: String, occurrenceScheduledAt: String? = null): TaskDto =
        api.reopenTask(taskId, CloseReopenRequest(occurrenceScheduledAt))

    suspend fun deleteTask(id: String, scope: RecurrenceScope? = null, occurrenceScheduledAt: String? = null) {
        val body = when (scope) {
            RecurrenceScope.THIS_ONLY -> DeleteTaskBody("THIS_ONLY", occurrenceScheduledAt)
            RecurrenceScope.FROM_THIS -> DeleteTaskBody("FROM_THIS", occurrenceScheduledAt)
            null -> DeleteTaskBody()
        }
        api.deleteTask(id, body)
    }
}
