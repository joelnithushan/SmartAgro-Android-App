package com.example.smartagro.domain.model.rtdb

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RtdbGasData(
    val co2: Int? = null,
    val nh3: Int? = null
)
