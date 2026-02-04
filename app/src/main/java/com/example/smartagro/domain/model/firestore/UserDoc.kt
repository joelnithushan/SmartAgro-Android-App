package com.example.smartagro.domain.model.firestore

data class UserDoc(
    val activeDeviceId: String? = null,
    val devices: List<String> = emptyList()
)

