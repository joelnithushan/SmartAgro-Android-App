package com.example.smartagro.domain.model.rtdb

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RelayStatusObject(
    val value: String? = null,
    val requestedBy: String? = null,
    val requestedByEmail: String? = null,
    val timestamp: Long? = null
) {
    fun normalizedValue(): RelayCommand? = RelayCommand.fromRaw(value)
}

sealed class RelayStatus {
    data class RawString(val value: RelayCommand) : RelayStatus()
    data class Object(val data: RelayStatusObject) : RelayStatus()

    fun valueOrNull(): RelayCommand? {
        return when (this) {
            is RawString -> value
            is Object -> data.normalizedValue()
        }
    }

    fun timestampOrNull(): Long? {
        return when (this) {
            is RawString -> null
            is Object -> data.timestamp
        }
    }

    companion object {
        fun parse(snapshot: DataSnapshot): RelayStatus? {
            val rawString = snapshot.getValue(String::class.java)
            val asCommand = RelayCommand.fromRaw(rawString)
            if (asCommand != null) return RawString(asCommand)

            val obj = snapshot.getValue(RelayStatusObject::class.java)
            if (obj != null && obj.value != null) return Object(obj)

            return null
        }
    }
}

