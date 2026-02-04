package com.example.smartagro.domain.model.rtdb

enum class RelayCommand(val raw: String) {
    ON("on"),
    OFF("off");

    companion object {
        fun fromRaw(raw: String?): RelayCommand? {
            return when (raw?.trim()?.lowercase()) {
                "on" -> ON
                "off" -> OFF
                else -> null
            }
        }
    }
}

