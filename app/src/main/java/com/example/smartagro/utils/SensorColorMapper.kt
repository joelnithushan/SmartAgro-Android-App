package com.example.smartagro.utils

import com.example.smartagro.R

/**
 * Maps sensor labels to their corresponding accent colors.
 * Each sensor parameter has a unique color for visual distinction.
 */
object SensorColorMapper {
    
    /**
     * Get accent color resource ID for a sensor label.
     * Returns a distinct color for each sensor type.
     */
    fun getAccentColorResId(label: String): Int {
        return when (label) {
            "Air Temp" -> R.color.accent_air_temp
            "Humidity" -> R.color.accent_humidity
            "Soil Moisture" -> R.color.accent_soil_moisture
            "Soil Moist Raw" -> R.color.accent_soil_raw
            "Soil Temp" -> R.color.accent_soil_temp
            "AQI", "Air Quality" -> R.color.accent_aqi
            "CO2" -> R.color.accent_co2
            "NH3" -> R.color.accent_nh3
            "Light" -> R.color.accent_light
            "Rain" -> R.color.accent_rain
            "Relay" -> R.color.accent_relay
            else -> R.color.accent_air_temp // Default fallback
        }
    }
    
    /**
     * Get icon chip background color (lighter version of accent color with transparency).
     * Returns a semi-transparent version of the accent color for the icon chip.
     */
    fun getIconChipBackgroundColor(label: String): String {
        return when (label) {
            "Air Temp" -> "#40FF6B35"      // Orange with transparency
            "Humidity" -> "#404A90E2"      // Blue with transparency
            "Soil Moisture" -> "#407ED321"  // Green with transparency
            "Soil Moist Raw" -> "#4050E3C2" // Teal with transparency
            "Soil Temp" -> "#40F5A623"      // Yellow-Orange with transparency
            "AQI", "Air Quality" -> "#40D0021B" // Red with transparency
            "CO2" -> "#409013FE"           // Purple with transparency
            "NH3" -> "#4000D9FF"           // Cyan with transparency
            "Light" -> "#40F8E71C"         // Yellow with transparency
            "Rain" -> "#404178BE"          // Blue with transparency
            "Relay" -> "#4050C878"         // Green with transparency
            else -> "#40FF6B35"            // Default fallback
        }
    }
    
    /**
     * Get card stroke color (accent color with transparency).
     * Returns a semi-transparent version of the accent color for card borders.
     */
    fun getCardStrokeColor(label: String): String {
        return when (label) {
            "Air Temp" -> "#60FF6B35"      // Orange with more transparency
            "Humidity" -> "#604A90E2"     // Blue with more transparency
            "Soil Moisture" -> "#607ED321" // Green with more transparency
            "Soil Moist Raw" -> "#6050E3C2" // Teal with more transparency
            "Soil Temp" -> "#60F5A623"     // Yellow-Orange with more transparency
            "AQI", "Air Quality" -> "#60D0021B" // Red with more transparency
            "CO2" -> "#609013FE"          // Purple with more transparency
            "NH3" -> "#6000D9FF"          // Cyan with more transparency
            "Light" -> "#60F8E71C"        // Yellow with more transparency
            "Rain" -> "#604178BE"         // Blue with more transparency
            "Relay" -> "#6050C878"        // Green with more transparency
            else -> "#60FF6B35"           // Default fallback
        }
    }
}
