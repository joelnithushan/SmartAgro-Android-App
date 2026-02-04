package com.example.smartagro.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class IrrigationStatus(
    val isOn: Boolean = false,
    val mode: IrrigationMode = IrrigationMode.MANUAL,
    val threshold: Double = 30.0,
    val lastActionTime: Long = 0L,
    val lastActionSource: ActionSource = ActionSource.MANUAL,
    val timestamp: Long = 0L
)

enum class IrrigationMode {
    MANUAL, AUTO
}

enum class ActionSource {
    MANUAL, AUTO
}
