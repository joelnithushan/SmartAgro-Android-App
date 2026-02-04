package com.example.smartagro.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SensorData(
    val soilMoisture: Double = 0.0,
    val soilTemp: Double = 0.0,
    val airTemp: Double = 0.0,
    val humidity: Double = 0.0,
    val rainLevel: Double = 0.0,
    val gasCO2: Double = 0.0,
    val light: Double = 0.0,
    val timestamp: Long = 0L
)

enum class SensorStatus {
    NORMAL, LOW, HIGH
}

data class SensorCardData(
    val label: String,
    val value: Double,
    val unit: String,
    val status: SensorStatus,
    val iconResId: Int
)
