package com.example.smartagro.utils

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeviceConfig {
    private const val PREFS_NAME = "smartagro_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    private const val DEFAULT_ID = "ESP32_001"

    private val _deviceId = MutableStateFlow(DEFAULT_ID)
    val deviceIdFlow: StateFlow<String> = _deviceId.asStateFlow()

    val currentDeviceId: String
        get() = _deviceId.value

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_DEVICE_ID, DEFAULT_ID) ?: DEFAULT_ID
        _deviceId.value = id
    }

    fun updateDeviceId(context: Context, newId: String) {
        val clean = newId.ifBlank { DEFAULT_ID }
        _deviceId.value = clean
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_DEVICE_ID, clean).apply()
    }
}

