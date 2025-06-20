package com.example.airzen.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.airzen.R
import com.example.airzen.util.Constants
import com.example.airzen.util.RetrofitInstance

class AirQualityWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val lat = inputData.getDouble("lat", 0.0)
        val lon = inputData.getDouble("lon", 0.0)

        return try {
            val response = RetrofitInstance.airQualityApi.getAirQuality(
                lat = lat,
                lon = lon,
                apiKey = Constants.OPEN_WEATHER_API_KEY
            )

            val aqi = response.list[0].main.aqi

            if (aqi >= 3) {
                sendNotification(aqi)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(aqi: Int) {
        val channelId = "airzen_alerts"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Kanal oluştur
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hava Kalitesi Uyarıları",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hava Kalitesi Uyarısı 🚨")
            .setContentText("AQI: $aqi — Dışarı çıkmaman önerilir.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1001, notification)
    }
}