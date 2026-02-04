package com.example.smartagro.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class SensorData(
    val airTemperature: Double = 0.0,
    val soilMoisture: Double = 0.0,
    val soilTemperature: Double = 0.0,
    val humidity: Double = 0.0,
    val soilPh: Double = 0.0,
    val airQuality: Double = 0.0,
    val co2Level: Double = 0.0,
    val nh3Level: Double = 0.0,
    val nitrogenLevel: Double = 0.0,
    val phosphorusLevel: Double = 0.0,
    val potassiumLevel: Double = 0.0,
    val lightLevel: Double = 0.0,
    val waterLevel: Double = 0.0,
    val timestamp: Long = 0L
)
