package com.example.airzen

import android.app.Application
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class AirZenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        MapLibre.getInstance(this)
    }
}