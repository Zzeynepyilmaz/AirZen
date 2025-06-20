package com.example.airzen.domain.model

data class WeatherResponse(
    val main: Main,
    val wind: Wind,
    val weather: List<WeatherDesc>
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Wind(
    val speed: Double
)

data class WeatherDesc(
    val description: String
)
