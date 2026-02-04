package com.example.smartagro.domain.model.rtdb

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RtdbSensorLatest(
    val soilMoisturePercent: Double? = null,
    val soilTempC: Double? = null,
    val airTempC: Double? = null,
    val humidityPercent: Double? = null,
    val rainLevelPercent: Double? = null,
    val gasPpm: Double? = null,
    val lightPercent: Double? = null,
    val relayStatus: String? = null,
    val updatedAt: Long? = null
) {
    fun toSnapshotDefaults(): com.example.smartagro.domain.model.SensorSnapshot {
        return com.example.smartagro.domain.model.SensorSnapshot(
            soilMoisturePercent = soilMoisturePercent ?: 0.0,
            soilTempC = soilTempC ?: 0.0,
            airTempC = airTempC ?: 0.0,
            humidityPercent = humidityPercent ?: 0.0,
            rainLevelPercent = rainLevelPercent ?: 0.0,
            gasPpm = gasPpm ?: 0.0,
            lightPercent = lightPercent ?: 0.0,
            updatedAt = updatedAt ?: 0L
        )
    }
}

