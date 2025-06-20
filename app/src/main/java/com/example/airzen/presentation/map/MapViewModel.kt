package com.example.airzen.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.airzen.R
import com.example.airzen.domain.model.AirQualityEntity
import com.example.airzen.domain.model.WeatherInfo
import com.example.airzen.presentation.widget.AirQualityWidget
import com.example.airzen.util.Constants
import com.example.airzen.util.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre.getApplicationContext

class MapViewModel : ViewModel() {

    private val _airQuality = MutableStateFlow(AirQualityState())
    val airQuality: StateFlow<AirQualityState> = _airQuality

    private val _shouldShowAlert = MutableStateFlow(false)
    val shouldShowAlert: StateFlow<Boolean> = _shouldShowAlert

    private val _history = mutableStateListOf<AirQualityEntity>()
    val history: List<AirQualityEntity> get() = _history

    private val _weatherInfo = MutableStateFlow(WeatherInfo())
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo
//    // TEST AMAÇLI: WeatherInfo state'ini doğrudan ayarlamak için
//    fun testSetWeatherInfo(info: WeatherInfo) {
//        _weatherInfo.value = info
//    }

    @SuppressLint("NewApi")
    fun addToHistory(item: AirQualityEntity) {
        _history.add(item)
        if (_history.size > 6) {
            _history.removeFirst()
        }
    }

    fun loadAirQuality(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.airQualityApi.getAirQuality(
                    lat = lat,
                    lon = lon,
                    apiKey = Constants.OPEN_WEATHER_API_KEY
                )

                val data = response.list[0]
                Log.d(
                    "AirZen",
                    "AQI: ${data.main.aqi}, PM2.5: ${data.components.pm2_5}, PM10: ${data.components.pm10}"
                )

                addToHistory(
                    AirQualityEntity(
                        pm25 = data.components.pm2_5,
                        pm10 = data.components.pm10,
                        aqi = data.main.aqi
                    )
                )

                val aqiValue = data.main.aqi
                _airQuality.value = AirQualityState(
                    aqi = data.main.aqi,
                    pm25 = data.components.pm2_5,
                    pm10 = data.components.pm10,
                    no2 = data.components.no2,
                    o3 = data.components.o3
                )

                val context = getApplicationContext()
                val prefs = context.getSharedPreferences("airzen_widget", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("message", getAQIDescription(data.main.aqi))
                    .putString("value", "PM2.5: ${data.components.pm2_5} µg/m³")
                    .putInt("icon", R.drawable.outline_warning_24)
                    .apply()

                AirQualityWidget.updateAllWidgets(context, getAQIDescription(data.main.aqi), "PM2.5: ${data.components.pm2_5} µg/m³", R.drawable.outline_warning_24)

                if (aqiValue >= 3) {
                    _shouldShowAlert.value = true
                }
            } catch (e: Exception) {
                // TODO: Hata durumu için geri bildirim eklenebilir
            }
        }
    }

    fun predictNextPM25(): Double {
        val pmValues = history.map { it.pm25 }
        if (pmValues.size < 2) return 0.0

        val diffs = pmValues.zipWithNext { a, b -> b - a }
        val avgDiff = diffs.average()

        return pmValues.last() + avgDiff
    }

    fun loadWeatherInfo(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.weatherApi.getWeather(
                    lat = lat,
                    lon = lon,
                    apiKey = Constants.OPEN_WEATHER_API_KEY,
                    units = "metric",
                    lang = "tr"
                )

                _weatherInfo.value = WeatherInfo(
                    temperature = response.main.temp,
                    humidity = response.main.humidity,
                    windSpeed = response.wind.speed,
                    description = response.weather[0].description
                )

            } catch (e: Exception) {
                Log.e("AirZen", "Hava durumu alınamadı: ${e.localizedMessage}")
            }
        }
    }

    fun dismissAlert() {
        _shouldShowAlert.value = false
    }
}