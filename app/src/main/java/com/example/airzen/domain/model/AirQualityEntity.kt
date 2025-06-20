package com.example.airzen.domain.model

data class AirQualityEntity(
    val timestamp: Long = System.currentTimeMillis(),
    val pm25: Float,
    val pm10: Float,
    val aqi: Int
)
