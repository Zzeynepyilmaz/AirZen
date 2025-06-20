package com.example.airzen.domain.model

data class AirQualityResponse(
    val list: List<AirData>
)

data class AirData(
    val main: MainData,
    val components: Components,
    val dt: Long
)

data class MainData(
    val aqi: Int
)

data class Components(
    val pm2_5: Float,
    val pm10: Float,
    val no2: Float,
    val o3: Float
)
