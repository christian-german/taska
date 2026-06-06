package com.taska.android.data.repository

import com.taska.android.data.api.RetrofitClient
import com.taska.android.data.api.TaskaApi
import com.taska.android.data.model.RegisterDeviceRequest

class DeviceRepository(private val api: TaskaApi) {

    constructor() : this(RetrofitClient.api)

    suspend fun registerDevice(request: RegisterDeviceRequest) = api.registerDevice(request)
}
