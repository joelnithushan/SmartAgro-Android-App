package com.example.smartagro.utils

import com.example.smartagro.R

/**
 * Maps sensor labels to their corresponding PNG icon resources.
 * These icons should be agriculture/IoT-style PNG images placed in res/drawable/
 */
object IconMapper {
    
    /**
     * Get icon resource ID for a sensor label.
     * Returns PNG icon resources for agriculture/IoT sensors.
     */
    fun getIconResId(label: String): Int {
        return when (label) {
            "Air Temp" -> R.drawable.ic_air_temp
            "Humidity" -> R.drawable.ic_humidity
            "Soil Moisture" -> R.drawable.ic_soil_moisture
            "Soil Moist Raw" -> R.drawable.ic_soil_raw
            "Soil Temp" -> R.drawable.ic_soil_temp
            "AQI", "Air Quality" -> R.drawable.ic_aqi
            "CO2" -> R.drawable.ic_co2
            "NH3" -> R.drawable.ic_nh3
            "Light" -> R.drawable.ic_light
            "Rain" -> R.drawable.ic_rain
            "Relay" -> R.drawable.ic_relay
            else -> R.drawable.ic_air_temp // Default fallback
        }
    }
}
