package com.pollyannawu.justwoo.data.datasource.auth

import com.russhwolf.settings.Settings
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun interface DeviceIdProvider {
    fun get(): String
}

@OptIn(ExperimentalUuidApi::class)
class DefaultDeviceIdProvider(
    private val settings: Settings,
) : DeviceIdProvider {

    override fun get(): String {
        settings.getStringOrNull(KEY_DEVICE_ID)?.let { return it }
        val id = Uuid.random().toString()
        settings.putString(KEY_DEVICE_ID, id)
        return id
    }

    private companion object {
        const val KEY_DEVICE_ID = "device.id"
    }
}
