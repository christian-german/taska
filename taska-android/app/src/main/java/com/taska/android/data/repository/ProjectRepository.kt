package com.taska.android.data.repository

import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.api.TaskaApi
import com.taska.android.data.model.ProjectDto

class ProjectRepository(private val api: TaskaApi) {

    constructor() : this(RetrofitClient.api)

    suspend fun getProjects(): List<ProjectDto> = api.getProjects()
}
