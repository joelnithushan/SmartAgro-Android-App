package com.example.smartagro.domain.model.firestore

data class UserDevice(
    val deviceId: String,
    val farmName: String? = null,
    val location: String? = null,
    val status: String? = null
)
