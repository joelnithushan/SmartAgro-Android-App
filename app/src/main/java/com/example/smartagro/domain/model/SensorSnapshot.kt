package com.example.smartagro.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SensorSnapshot(
    val soilMoisturePercent: Double = 0.0,
    val soilTempC: Double = 0.0,
    val airTempC: Double = 0.0,
    val humidityPercent: Double = 0.0,
    val rainLevelPercent: Double = 0.0,
    val gasPpm: Double = 0.0,
    val lightPercent: Double = 0.0,
    val updatedAt: Long = 0L
)
