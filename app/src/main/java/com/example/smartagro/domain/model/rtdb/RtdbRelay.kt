package com.example.smartagro.domain.model.rtdb

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RtdbRelay(
    val status: Any? = null,
    val mode: String? = null,
    val lastChangedBy: String? = null,
    val timestamp: Long? = null
)

