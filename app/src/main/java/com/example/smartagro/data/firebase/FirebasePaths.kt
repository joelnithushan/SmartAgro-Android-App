package com.example.smartagro.data.firebase

object FirebasePaths {
    private const val FARMS = "farms"
    private const val SENSORS = "sensors"
    private const val CURRENT = "current"
    private const val IRRIGATION = "irrigation"
    
    fun sensorsPath(farmId: String): String {
        return "$FARMS/$farmId/$SENSORS/$CURRENT"
    }
    
    fun irrigationPath(farmId: String): String {
        return "$FARMS/$farmId/$IRRIGATION"
    }
}
