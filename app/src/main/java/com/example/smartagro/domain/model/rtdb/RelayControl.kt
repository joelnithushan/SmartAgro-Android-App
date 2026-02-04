package com.example.smartagro.domain.model.rtdb

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RelayControl(
    val status: RelayStatus? = null,
    val mode: String? = null,
    val lastChangedBy: String? = null,
    val timestamp: Long? = null
) {
    companion object {
        fun fromRtdbRelay(rtdbRelay: RtdbRelay, statusSnapshot: DataSnapshot?): RelayControl {
            val parsedStatus = statusSnapshot?.let { RelayStatus.parse(it) }
            return RelayControl(
                status = parsedStatus,
                mode = rtdbRelay.mode,
                lastChangedBy = rtdbRelay.lastChangedBy,
                timestamp = rtdbRelay.timestamp
            )
        }
    }
}
