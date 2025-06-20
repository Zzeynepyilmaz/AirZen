package com.example.airzen.presentation.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.airzen.worker.AirQualityWorker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import java.util.concurrent.TimeUnit

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val notificationPermissionState =
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

    val viewModel: MapViewModel = viewModel()
    val airState by viewModel.airQuality.collectAsState()
    val showAlert by viewModel.shouldShowAlert.collectAsState()
    val predictedPM25 = viewModel.predictNextPM25()
    val weatherState by viewModel.weatherInfo.collectAsState()
    val history = viewModel.history

    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
        if (!notificationPermissionState.status.isGranted) {
            notificationPermissionState.launchPermissionRequest()
        }
    }

    val mapView = remember {
        MapView(context).apply {
            getMapAsync { map ->
                map.setStyle("https://tile.ankageo.com/styles/anka-light/style.json") { style ->
                    map.locationComponent.apply {
                        activateLocationComponent(
                            LocationComponentActivationOptions.builder(context, style)
                                .useDefaultLocationEngine(true)
                                .build()
                        )
                        isLocationComponentEnabled = true
                        cameraMode = CameraMode.TRACKING
                        renderMode = RenderMode.COMPASS

                        lastKnownLocation?.let { location ->
                            viewModel.loadAirQuality(location.latitude, location.longitude)
                            viewModel.loadWeatherInfo(location.latitude, location.longitude)
                            scheduleAirQualityWorker(context, location.latitude, location.longitude)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.95f), Color.LightGray.copy(alpha = 0.6f))
                    )
                ),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Genel") },
                        icon = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Geçmiş") },
                        icon = { Icon(Icons.Default.History, contentDescription = "Geçmiş") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Hava Durumu") },
                        icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTabIndex) {
                    0 -> Column {
                        Text("PM2.5: ${airState.pm25}")
                        Text("PM10: ${airState.pm10}")
                        Text("NO₂: ${airState.no2}")
                        Text("O₃: ${airState.o3}")
                        Text("Tahmini PM2.5: ${"%.2f".format(predictedPM25)}")
                    }

                    1 -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        history.forEach {
                            Text("${it.pm25.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    2 -> Column {
                        Text("🌡️ Sıcaklık: ${weatherState.temperature}°C")
                        Text("💧 Nem: ${weatherState.humidity}%")
                        Text("💨 Rüzgar: ${weatherState.windSpeed} m/s")
                        Text("☁️ Açıklama: ${weatherState.description}")
                    }
                }
            }
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissAlert() },
                title = { Text("Hava Kalitesi Uyarısı 🚨") },
                text = { Text("Bulunduğun bölgede hava kalitesi sağlıksız. Dışarıda uzun süre kalmaman önerilir.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissAlert() }) {
                        Text("Tamam")
                    }
                }
            )
        }
    }
}

fun scheduleAirQualityWorker(context: Context, lat: Double, lon: Double) {
    val input = workDataOf("lat" to lat, "lon" to lon)
    val request = PeriodicWorkRequestBuilder<AirQualityWorker>(15, TimeUnit.MINUTES)
        .setInputData(input)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "airQualityCheck",
        ExistingPeriodicWorkPolicy.UPDATE,
        request
    )
}
