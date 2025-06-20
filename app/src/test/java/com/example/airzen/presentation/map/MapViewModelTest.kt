package com.example.airzen.presentation.map

import com.example.airzen.domain.model.AirQualityEntity
import com.example.airzen.domain.model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MapViewModelTest {

    private lateinit var viewModel: MapViewModel

    @Before
    fun setup() {
        viewModel = MapViewModel()
    }

    @Test
    fun `predictNextPM25 returns 0 when history is empty`() {
        val result = viewModel.predictNextPM25()
        assertEquals(0.0f, result.toFloat(), 0.001f)
    }

    @Test
    fun `predictNextPM25 returns trend-based prediction`() {
        val testHistory = listOf(
            AirQualityEntity(aqi = 1, pm25 = 10f, pm10 = 15f),
            AirQualityEntity(aqi = 2, pm25 = 20f, pm10 = 25f),
            AirQualityEntity(aqi = 2, pm25 = 30f, pm10 = 30f),
            AirQualityEntity(aqi = 3, pm25 = 40f, pm10 = 35f),
            AirQualityEntity(aqi = 3, pm25 = 50f, pm10 = 40f),
            AirQualityEntity(aqi = 4, pm25 = 60f, pm10 = 45f)
        )

        testHistory.forEach { viewModel.addToHistory(it) }

        val result = viewModel.predictNextPM25()
        val expected = 70.0

        assertEquals(expected, result, 0.001)
    }

    @Test
    fun `addToHistory keeps only the latest 6 items`() {
        repeat(8) { i ->
            viewModel.addToHistory(
                AirQualityEntity(aqi = 1, pm25 = i * 10f, pm10 = 20f)
            )
        }

        val history = viewModel.history

        assertEquals(6, history.size)
        assertEquals(20f, history.first().pm25)
        assertEquals(70f, history.last().pm25)
    }

    @Test
    fun `default weatherInfo is empty`() {
        val defaultInfo = viewModel.weatherInfo.value
        assertEquals(0.0, defaultInfo.temperature, 0.01)
        assertEquals(0, defaultInfo.humidity)
        assertEquals(0.0, defaultInfo.windSpeed, 0.01)
        assertEquals("", defaultInfo.description)
    }

    @Test
    fun `weatherInfo state is updated manually`() {
        val testInfo = WeatherInfo(
            temperature = 22.5,
            humidity = 60,
            windSpeed = 5.4,
            description = "parçalı bulutlu"
        )

//        viewModel.testSetWeatherInfo(testInfo)

        val result = viewModel.weatherInfo.value
        assertEquals(22.5, result.temperature, 0.01)
        assertEquals(60, result.humidity)
        assertEquals(5.4, result.windSpeed, 0.01)
        assertEquals("parçalı bulutlu", result.description)
    }

    @Test
    fun `dismissAlert sets shouldShowAlert to false`() {
        viewModel.apply {
            // alert'i açık varsay
            @Suppress("UNCHECKED_CAST")
            (this::class.java.getDeclaredField("_shouldShowAlert").apply { isAccessible = true }
                .get(this) as MutableStateFlow<Boolean>).value = true

            dismissAlert()
        }

        assertEquals(false, viewModel.shouldShowAlert.value)
    }

}