package com.example.airzen.presentation.map

data class AirQualityState(
    val aqi: Int = 0,
    val pm25: Float = 0f,
    val pm10: Float = 0f,
    val no2: Float = 0f,
    val o3: Float = 0f
)
