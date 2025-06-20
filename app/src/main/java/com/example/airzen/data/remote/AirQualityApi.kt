package com.example.airzen.data.remote

import com.example.airzen.domain.model.AirQualityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApi {
    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirQualityResponse
}