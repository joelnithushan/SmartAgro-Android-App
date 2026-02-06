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
            "Air Temp" -> "#50FF6B35"      // Orange with increased opacity (31% opacity) for white background
            "Humidity" -> "#504A90E2"      // Blue with increased opacity
            "Soil Moisture" -> "#507ED321"  // Green with increased opacity
            "Soil Moist Raw" -> "#5050E3C2" // Teal with increased opacity
            "Soil Temp" -> "#50F5A623"      // Yellow-Orange with increased opacity
            "AQI", "Air Quality" -> "#50D0021B" // Red with increased opacity
            "CO2" -> "#509013FE"           // Purple with increased opacity
            "NH3" -> "#5000D9FF"           // Cyan with increased opacity
            "Light" -> "#50F8E71C"         // Yellow with increased opacity
            "Rain" -> "#504178BE"          // Blue with increased opacity
            "Relay" -> "#5050C878"         // Green with increased opacity
            else -> "#50FF6B35"            // Default fallback
        }
    }
    
    /**
     * Get card stroke color (accent color with transparency).
     * Returns a semi-transparent version of the accent color for card borders.
     */
    fun getCardStrokeColor(label: String): String {
        return when (label) {
            "Air Temp" -> "#40FF6B35"      // Orange with more transparency (40% opacity)
            "Humidity" -> "#404A90E2"     // Blue with more transparency
            "Soil Moisture" -> "#407ED321" // Green with more transparency
            "Soil Moist Raw" -> "#4050E3C2" // Teal with more transparency
            "Soil Temp" -> "#40F5A623"     // Yellow-Orange with more transparency
            "AQI", "Air Quality" -> "#40D0021B" // Red with more transparency
            "CO2" -> "#409013FE"          // Purple with more transparency
            "NH3" -> "#4000D9FF"          // Cyan with more transparency
            "Light" -> "#40F8E71C"        // Yellow with more transparency
            "Rain" -> "#404178BE"         // Blue with more transparency
            "Relay" -> "#4050C878"        // Green with more transparency
            else -> "#40FF6B35"           // Default fallback
        }
    }
}
