package com.example.smartagro.domain.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class IrrigationStatus(
    val autoMode: Boolean = false,
    val manualMode: Boolean = false,
    val soilMoisture: Double = 0.0,
    val threshold: Double = 30.0,
    val pump1Status: Boolean = false,
    val pump2Status: Boolean = false,
    val duration: Int = 30,
    val morningSchedule: String = "06:00 AM",
    val eveningSchedule: String = "06:00 PM",
    val systemActive: Boolean = true,
    val lastWatered: Long = 0L,
    val timestamp: Long = 0L
)
