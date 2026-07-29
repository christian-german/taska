package com.taska.android.data.repository

import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.api.TaskaApi
import com.taska.android.data.model.TimeEntryRequest

class TimeEntryRepository(private val api: TaskaApi) {

    constructor() : this(RetrofitClient.api)

    suspend fun createTimeEntry(request: TimeEntryRequest) = api.createTimeEntry(request)
}
