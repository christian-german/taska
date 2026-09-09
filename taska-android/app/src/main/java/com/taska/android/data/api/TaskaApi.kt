package com.taska.android.data.api

import com.taska.android.data.model.CloseReopenRequest
import com.taska.android.data.model.DeleteTaskBody
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.OccurrenceUpdateRequest
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.RegisterDeviceRequest
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.model.TaskUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TaskaApi {

    @GET("/projects")
    suspend fun getProjects(): List<ProjectDto>

    @GET("/tasks")
    suspend fun getTasks(
        @Query("projectId") projectId: String? = null,
        @Query("showCompleted") showCompleted: Boolean = false,
        @Query("date") date: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
    ): List<TaskDto>

    @GET("/tasks/{taskId}")
    suspend fun getTask(@Path("taskId") taskId: String): TaskDto

    @GET("/tasks/{taskId}/subtasks")
    suspend fun getSubtasks(@Path("taskId") taskId: String): List<TaskDto>

    @POST("/tasks")
    suspend fun createTask(@Body request: TaskRequest): TaskDto

    @PUT("/tasks/{taskId}")
    suspend fun updateTask(@Path("taskId") taskId: String, @Body request: TaskUpdateRequest): TaskDto

    @PUT("/tasks/{taskId}/occurrences/{occurrenceScheduledAt}/following")
    suspend fun updateFollowingTask(
        @Path("taskId") taskId: String,
        @Path("occurrenceScheduledAt") occurrenceScheduledAt: String,
        @Body request: TaskUpdateRequest,
    ): TaskDto

    @PUT("/tasks/{taskId}/occurrences/{occurrenceScheduledAt}")
    suspend fun updateOccurrence(
        @Path("taskId") taskId: String,
        @Path("occurrenceScheduledAt") occurrenceScheduledAt: String,
        @Body request: OccurrenceUpdateRequest,
    ): TaskDto

    @POST("/tasks/{taskId}/close")
    suspend fun closeTask(
        @Path("taskId") taskId: String,
        @Body body: CloseReopenRequest = CloseReopenRequest(),
    ): TaskDto

    @POST("/tasks/{taskId}/reopen")
    suspend fun reopenTask(
        @Path("taskId") taskId: String,
        @Body body: CloseReopenRequest = CloseReopenRequest(),
    ): TaskDto

    @HTTP(method = "DELETE", path = "/tasks/{taskId}", hasBody = true)
    suspend fun deleteTask(
        @Path("taskId") taskId: String,
        @Body body: DeleteTaskBody = DeleteTaskBody(),
    )

    @GET("/labels")
    suspend fun getLabels(): List<LabelDto>

    @POST("/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest)
}
