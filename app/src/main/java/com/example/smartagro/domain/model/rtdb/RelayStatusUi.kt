package com.example.smartagro.domain.model.rtdb

data class RelayStatusUi(
    val value: RelayCommand?,
    val requestedBy: String?,
    val requestedByEmail: String?,
    val timestamp: Long?
) {
    companion object {
        fun fromRelayStatus(status: RelayStatus?): RelayStatusUi {
            return when (status) {
                is RelayStatus.RawString -> RelayStatusUi(
                    value = status.value,
                    requestedBy = null,
                    requestedByEmail = null,
                    timestamp = null
                )
                is RelayStatus.Object -> RelayStatusUi(
                    value = status.data.normalizedValue(),
                    requestedBy = status.data.requestedBy,
                    requestedByEmail = status.data.requestedByEmail,
                    timestamp = status.data.timestamp
                )
                null -> RelayStatusUi(
                    value = null,
                    requestedBy = null,
                    requestedByEmail = null,
                    timestamp = null
                )
            }
        }
    }
}
