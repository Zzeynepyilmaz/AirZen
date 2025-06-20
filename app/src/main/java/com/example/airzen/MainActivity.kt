package com.example.airzen

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.airzen.presentation.map.MapScreen
import com.example.airzen.ui.theme.AirZenTheme
import com.example.airzen.worker.AirQualityWorker
import com.google.android.gms.location.LocationServices
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val periodicWork = PeriodicWorkRequestBuilder<AirQualityWorker>(
                    2, TimeUnit.HOURS
                )
                    .setInputData(
                        workDataOf(
                            "lat" to location.latitude,
                            "lon" to location.longitude
                        )
                    )
                    .build()

                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "air_quality_check",
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
                )
            }
        }
        setContent {
            AirZenTheme {
                MapScreen()
            }
        }
    }
}
