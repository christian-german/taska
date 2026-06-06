package com.taska.android.data.api

import com.taska.android.data.model.CloseReopenRequest
import com.taska.android.data.model.DeleteTaskBody
import com.taska.android.data.model.LabelDto
import com.taska.android.data.model.ProjectDto
import com.taska.android.data.model.RegisterDeviceRequest
import com.taska.android.data.model.TaskDto
import com.taska.android.data.model.TaskRequest
import com.taska.android.data.model.TimeEntryRequest
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
        @Query("project_id") projectId: String? = null,
        @Query("show_completed") showCompleted: Boolean = false,
        @Query("date") date: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): List<TaskDto>

    @GET("/tasks/{id}")
    suspend fun getTask(@Path("id") id: String): TaskDto

    @GET("/tasks/{taskId}/subtasks")
    suspend fun getSubtasks(@Path("taskId") taskId: String): List<TaskDto>

    @POST("/tasks")
    suspend fun createTask(@Body request: TaskRequest): TaskDto

    @PUT("/tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body request: TaskRequest): TaskDto

    @POST("/tasks/{taskId}/close")
    suspend fun closeTask(
        @Path("taskId") taskId: String,
        @Body body: CloseReopenRequest = CloseReopenRequest()
    ): TaskDto

    @POST("/tasks/{taskId}/reopen")
    suspend fun reopenTask(
        @Path("taskId") taskId: String,
        @Body body: CloseReopenRequest = CloseReopenRequest()
    ): TaskDto

    @HTTP(method = "DELETE", path = "/tasks/{id}", hasBody = true)
    suspend fun deleteTask(@Path("id") id: String, @Body body: DeleteTaskBody = DeleteTaskBody())

    @POST("/time-entries")
    suspend fun createTimeEntry(@Body request: TimeEntryRequest)

    @GET("/labels")
    suspend fun getLabels(): List<LabelDto>

    @POST("/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest)
}
