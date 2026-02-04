package com.example.smartagro.utils

object Constants {
    var DEFAULT_FARM_ID = "farm_001"
        private set

    const val ENABLE_WRITE_ACTIVE_DEVICE_ID = false
    
    fun setFarmId(farmId: String) {
        DEFAULT_FARM_ID = farmId
    }
}
