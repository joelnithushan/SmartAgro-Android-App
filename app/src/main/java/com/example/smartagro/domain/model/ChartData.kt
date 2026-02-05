package com.example.smartagro.domain.model

data class ChartData(
    val tempHumidityData: List<Pair<Float, Pair<Float, Float>>>, // (timeIndex, (temp, humidity))
    val soilAqiData: List<Pair<Float, Pair<Float, Float>>> // (timeIndex, (soilMoisture, aqi))
)
