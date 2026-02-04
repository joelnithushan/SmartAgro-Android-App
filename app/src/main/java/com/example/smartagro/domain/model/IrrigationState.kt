package com.example.smartagro.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class IrrigationState(
    val isOn: Boolean = false,
    val mode: String = "MANUAL",
    val moistureThreshold: Double = 30.0,
    val lastChangedAt: Long = 0L,
    val lastChangedBy: String = "MANUAL"
)
