package com.example.smartagro.domain.model.rtdb

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RtdbSensorLatest(
    val soilMoistureRaw: Int? = null,
    val soilMoisturePct: Int? = null,
    val airTemperature: Double? = null,
    val airHumidity: Double? = null,
    val soilTemperature: Double? = null,
    val airQualityIndex: Int? = null,
    val gases: RtdbGasData? = null,
    val lightDetected: Int? = null,
    val rainLevelRaw: Int? = null,
    val rainStatus: String? = null,
    val relayStatus: String? = null,
    val timestamp: Long? = null
) {
    fun toSnapshotDefaults(): com.example.smartagro.domain.model.SensorSnapshot {
        val rainLevelPercent = rainLevelRaw?.let { raw ->
            when {
                raw > 3800 -> 0.0
                raw < 3000 -> 100.0
                else -> mapOf(3800.0 to 0.0, 3000.0 to 100.0).let { 
                    val percent = ((3800.0 - raw) / 800.0) * 100.0
                    percent.coerceIn(0.0, 100.0)
                }
            }
        } ?: 0.0

        val lightPercent = lightDetected?.let { 
            if (it == 1) 100.0 else 0.0 
        } ?: 0.0

        return com.example.smartagro.domain.model.SensorSnapshot(
            soilMoisturePercent = soilMoisturePct?.toDouble() ?: 0.0,
            soilTempC = soilTemperature ?: 0.0,
            airTempC = airTemperature ?: 0.0,
            humidityPercent = airHumidity ?: 0.0,
            rainLevelPercent = rainLevelPercent,
            gasPpm = gases?.co2?.toDouble() ?: 0.0,
            lightPercent = lightPercent,
            updatedAt = timestamp ?: 0L
        )
    }

    fun getCO2Ppm(): Int = gases?.co2 ?: 0
    fun getNH3Ppm(): Int = gases?.nh3 ?: 0
}

