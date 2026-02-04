package com.example.smartagro.utils

object Constants {
    var DEFAULT_FARM_ID = "farm_001"
        private set
    
    fun setFarmId(farmId: String) {
        DEFAULT_FARM_ID = farmId
    }
}
