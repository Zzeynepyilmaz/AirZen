package com.example.airzen.domain.model

data class WeatherInfo(
    val temperature: Double = 0.0,
    val humidity: Int = 0,
    val windSpeed: Double = 0.0,
    val description: String = ""
)